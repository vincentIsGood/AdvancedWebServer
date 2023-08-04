package com.vincentcodes.webserver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLParameters;

import com.vincentcodes.net.UpgradableSocket;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.HttpRequestValidator;
import com.vincentcodes.webserver.component.request.RequestParser;
import com.vincentcodes.webserver.component.response.HttpResponses;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.helper.IOContainer;
import com.vincentcodes.webserver.util.HttpRedirecter;

public class ServerThread extends Thread{
    private final WebServer.Configuration serverConfig;
    private UpgradableSocket clientConnection;
    private String clientIpWithPort;
    private HttpRequestValidator requestValidator;
    private HttpRequestDispatcher requestDispatcher;

    /**
     * Previous HttpRequest is always closed
     */
    private HttpRequest previousHttpRequest;

    private IOContainer socketIOContainer;
    
    private WebProtocol currentProtocol = WebProtocol.HTTP_ORIGINAL;

    /**
     * Creates a new server thread which deals with ONE TASK, which is handling an http request
     * @param clientConnection
     * @param requestValidator
     * @param requestDispatcher
     */
    public ServerThread(UpgradableSocket clientConnection, WebServer.Configuration configuration, HttpRequestValidator requestValidator, HttpRequestDispatcher requestDispatcher){
        this.clientConnection = clientConnection;
        this.requestValidator = requestValidator;
        this.requestDispatcher = requestDispatcher;
        this.serverConfig = configuration;

        this.clientIpWithPort = clientConnection.getInetAddress().getHostAddress() + ":" + clientConnection.getPort();
    }

    /**
     * @return whether configuration is successful
     */
    private boolean configureConnection() throws IOException{
        clientConnection.setClientMode(false);
        clientConnection.setSSLConfiguerer(this::configureSSL);

        boolean isClientUsingSSL = false;
        try{
            isClientUsingSSL = clientConnection.upgrade();
            // sslUpgrader is null => keep false
        }catch(NullPointerException ignore){}

        // client uses http but server uses https scheme
        if(!isClientUsingSSL && serverConfig.useSSL()){
            // redirect user back to https scheme
            HttpRequest request = RequestParser.parse(clientConnection.getInputStream());
            String newPath = "https://" + request.getHeaders().getHeader("host") + HttpRequest.rebuildPath(request.getBasicInfo());
            ResponseBuilder response = HttpResponses.createDefault();
            HttpRedirecter.permanentRedirect(request, response, newPath);
            httpReplyClient(response, clientConnection.getOutputStream());
            return false;
        }

        this.socketIOContainer = new IOContainer(clientConnection, 
            new BufferedInputStream(clientConnection.getInputStream()), 
            clientConnection.getOutputStream());
        clientConnection.getUnderlyingSocket().setSoTimeout(WebServer.CONNECTION_READ_TIMEOUT_MILSEC);
        return true;
    }
    private void configureSSL(SSLParameters sslParams){
        // SSL ALPN enabled protocols are set here...
        // Application-Layer Protocol Negotiation
        // https://tools.ietf.org/html/rfc7540#section-3.5
        //
        // h2c will not be implemented.
        if(serverConfig.forceHttp2())
            sslParams.setApplicationProtocols(new String[]{"h2", "http/1.1"}); // prefer h2
        else sslParams.setApplicationProtocols(new String[]{"http/1.1"});
    }

    public void run(){
        WebServer.logger.debug("Connection incoming from " + clientIpWithPort);
        try{
            // check if the user is using SSL or HTTP/1.1
            //
            // if server enforces HTTPS, redirect HTTP/1.1 clients to HTTPS scheme
            if(!configureConnection())
                return;

            ResponseBuilder response = handleHttpConnection();

            if(currentProtocol == WebProtocol.HTTP_TWO){
                ServerThreadUtils.http2Initialization(socketIOContainer, requestValidator, requestDispatcher);
            }else if(currentProtocol == WebProtocol.WEB_SOCKET){
                ServerThreadUtils.websocketInitialization(previousHttpRequest, socketIOContainer);
            }else if(currentProtocol == WebProtocol.TUNNEL){
                ServerThreadUtils.socketTunnelInitialization(previousHttpRequest, response, this::httpReplyClient);
            }
        }catch(Exception e){
            WebServer.logger.err("Catching a "+e.getClass().getName()+": " + e.getMessage());
            if(!WebServer.canIgnoreException(e))
                e.printStackTrace();
        }finally{
            try{
                clientConnection.close();
                WebServer.logger.debug("Connection with host " + clientIpWithPort + " closed");
            }catch(IOException e){
                WebServer.logger.err("Cannot close connection with host " + clientIpWithPort);
                e.printStackTrace();
                throw new UncheckedIOException(e);
            }
        }
        return;
    }

    /**
     * HTTP/1.1 handler. Dispatcher works here. Sends server response here.
     * @return Response corresponding to the request coming from the client for further processing.
     */
    private ResponseBuilder handleHttpConnection() throws SocketTimeoutException, IOException, InvocationTargetException{
        InputStream is = socketIOContainer.getInputStream();
        OutputStream os = socketIOContainer.getOutputStream();
        final ResponseBuilder response;

        try(HttpRequest request = RequestParser.parse(is)){
            request.setSocket(this.socketIOContainer);
            previousHttpRequest = request;
            if(!requestValidator.requestIsValid(request))
                request.invalid();
            
            dispatch:
            if(request.isValid()){
                response = requestDispatcher.dispatchObjectToHandlers(request);

                if(response.getHeaders().getHeader("X-Vws-Raw-Tunnel") != null){
                    currentProtocol = WebProtocol.TUNNEL;
                    break dispatch;
                }

                // upgrade stuff (both party agrees to upgrade)
                String responseUpgradeHeader = response.getHeaders().getHeader("upgrade");
                String requestUpgradeHeader = request.getHeaders().getHeader("upgrade");
                String currentAppProtocol = clientConnection.getApplicationProtocol();
                if(responseUpgradeHeader != null && requestUpgradeHeader != null){
                    if(responseUpgradeHeader.equals("websocket") && requestUpgradeHeader.equals("websocket"))
                        currentProtocol = WebProtocol.WEB_SOCKET;
                }else if(currentAppProtocol != null && currentAppProtocol.equals("h2")){
                    currentProtocol = WebProtocol.HTTP_TWO;
                }
            }else{
                response = HttpResponses.generate400Response();
            }
        }

        if(currentProtocol != WebProtocol.TUNNEL)
            httpReplyClient(response, os);

        // in http2, websocket they have their own ping/pong mechanism
        clientConnection.getUnderlyingSocket().setSoTimeout(0);
        return response;
    }
    /**
     * @param response [be closed] response to be sent
     * @param os send response to this output stream
     */
    private void httpReplyClient(ResponseBuilder response, OutputStream os){
        try(response){
            String resHeadersString = response.asString();
            os.write(resHeadersString.getBytes());
            WebServer.logger.debug("\n" + resHeadersString.replaceAll("\r\n", "\n"));
            
            if(response.getBody().length() > 0){
                TimeoutThread writeTimeout = initWriteTimeout(os, WebServer.CONNECTION_WRITE_TIMEOUT_MILSEC);
                // os.write(response.getBody().getBytes());
                response.getBody().streamBytesTo(os);
                writeTimeout.tryStop();
            }
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @return a thread which has already began its execution
     */
    private TimeoutThread initWriteTimeout(OutputStream os, int milsec){
        TimeoutThread timeoutThread = new TimeoutThread(milsec, (isThreadStopping)->{
            if(isThreadStopping) return;
            if(currentProtocol == WebProtocol.HTTP_ORIGINAL)
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        });
        timeoutThread.setDaemon(true);
        timeoutThread.start();
        return timeoutThread;
    }
}
