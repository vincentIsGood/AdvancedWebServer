package com.vincentcodes.webserver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;
import java.util.Optional;

import javax.net.ssl.SSLParameters;

import com.vincentcodes.net.UpgradableSocket;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.HttpRequestValidator;
import com.vincentcodes.webserver.component.request.RequestParser;
import com.vincentcodes.webserver.component.response.HttpResponses;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.exception.CannotParseRequestException;
import com.vincentcodes.webserver.helper.IOContainer;
import com.vincentcodes.webserver.http2.Http2Connection;
import com.vincentcodes.webserver.util.HttpRedirecter;
import com.vincentcodes.websocket.WebSocket;
import com.vincentcodes.websocket.WebSocketHandlerSelector;
import com.vincentcodes.websocket.handler.WebSocketOperator;

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
        boolean isClientUsingSSL = clientConnection.upgrade();

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

            handleHttpConnection();

            // upgrades are defined inside #handleHttpConnection() 
            if(currentProtocol == WebProtocol.HTTP_TWO){
                http2Initialization();
            }else if(currentProtocol == WebProtocol.WEB_SOCKET){
                websocketInitialization();
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

    // -------------- Different Protocol Handlers -------------- //
    private void handleHttpConnection() throws SocketTimeoutException, IOException, InvocationTargetException{
        InputStream is = socketIOContainer.getInputStream();
        OutputStream os = socketIOContainer.getOutputStream();
        final ResponseBuilder response;

        try(HttpRequest request = RequestParser.parse(is)){
            previousHttpRequest = request;
            if(!requestValidator.requestIsValid(request))
                request.invalid();
            
            if(request.isValid()){
                response = requestDispatcher.dispatchObjectToHandlers(request);

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

        httpReplyClient(response, os);

        // in http2, websocket they have their own ping/pong mechanism
        clientConnection.getUnderlyingSocket().setSoTimeout(0);
    }
    /**
     * @param response [be closed] response to be sent
     * @param os send response to this output stream
     */
    private void httpReplyClient(ResponseBuilder response, OutputStream os) throws IOException{
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
        }
    }

    /**
     * Still in experimental phase
     */
    private void http2Initialization() throws IOException, CannotParseRequestException, InvocationTargetException{
        WebServer.logger.debug("Debug messages for http2 are not available, modify the source code to turn it back on.");
        Http2Connection http2Connection = new Http2Connection(socketIOContainer, requestValidator, requestDispatcher);
        http2Connection.setup();
        http2Connection.takeover();
    }

    /**
     * WebSocket is supported. But it still needs more testings
     */
    private void websocketInitialization() throws IOException, ReflectiveOperationException{
        WebSocketHandlerSelector selector = new WebSocketHandlerSelector();
        WebSocket ws = new WebSocket(previousHttpRequest.getBasicInfo().getPath(), socketIOContainer);

        Optional<WebSocketOperator> chosenOperator = selector.searchForHandler(ws);
        if(chosenOperator.isPresent()){
            WebSocketConnection connection = new WebSocketConnection(ws, chosenOperator.get().getClass());
            try{
                connection.init();
                connection.start();
            }finally{
                connection.close();
            }
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
        timeoutThread.start();
        return timeoutThread;
    }
}
