package com.vincentcodes.webserver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;

import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.HttpRequestValidator;
import com.vincentcodes.webserver.component.request.RequestParser;
import com.vincentcodes.webserver.component.response.HttpResponses;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.exception.CannotParseRequestException;
import com.vincentcodes.webserver.helper.IOContainer;
import com.vincentcodes.webserver.http2.Http2Connection;
import com.vincentcodes.websocket.WebSocket;
import com.vincentcodes.websocket.WebSocketHandlerSelector;
import com.vincentcodes.websocket.handler.WebSocketOperator;

public class ServerThread extends Thread{
    private final WebServer.Configuration configuration;
    private Socket clientConnection;
    private SSLSocket sslConnection;
    private String clientIpWithPort;
    private HttpRequestValidator requestValidator;
    private HttpRequestDispatcher requestDispatcher;
    private HttpRequest previousHttpRequest;

    private IOContainer socketIOContainer;
    
    private WebProtocol currentProtocol = WebProtocol.HTTP_ORIGINAL;

    /**
     * Creates a new server thread which deals with ONE TASK, which is handling an http request
     * @param clientConnection This can be {SSLSocket | Socket}
     * @param requestValidator
     * @param requestDispatcher
     */
    public ServerThread(Socket clientConnection, WebServer.Configuration configuration, HttpRequestValidator requestValidator, HttpRequestDispatcher requestDispatcher){
        this.clientConnection = clientConnection;
        this.requestValidator = requestValidator;
        this.requestDispatcher = requestDispatcher;
        this.configuration = configuration;

        this.clientIpWithPort = clientConnection.getInetAddress().getHostAddress() + ":" + clientConnection.getPort();
    }

    // May create a new class out of these configurations?
    private void configureConnection() throws IOException{
        this.socketIOContainer = new IOContainer(clientConnection, 
            new BufferedInputStream(clientConnection.getInputStream()), 
            clientConnection.getOutputStream());
        clientConnection.setSoTimeout(WebServer.CONNECTION_READ_TIMEOUT_MILSEC);
    }
    private void configureSSL() throws IOException{
        // SSL ALPN enabled protocols are set here...
        // Application-Layer Protocol Negotiation
        // https://tools.ietf.org/html/rfc7540#section-3.5
        if(clientConnection instanceof SSLSocket){
            sslConnection = ((SSLSocket)clientConnection);
            SSLParameters sslParams = sslConnection.getSSLParameters();
            // h2c will not be implemented.
            if(configuration.forceHttp2())
                sslParams.setApplicationProtocols(new String[]{"h2", "http/1.1"}); // prefer h2
            else
                sslParams.setApplicationProtocols(new String[]{"http/1.1"});
            sslConnection.setSSLParameters(sslParams);
            sslConnection.startHandshake();
        }
    }

    public void run(){
        WebServer.logger.debug("Connection incoming from " + clientIpWithPort);
        try{
            configureConnection();
            configureSSL();

            handleHttpConnection();

            // upgrades are defined inside #handleHttpConnection() 
            if(currentProtocol == WebProtocol.HTTP_TWO){
                http2Initialization();
            }else if(currentProtocol == WebProtocol.WEB_SOCKET){
                websocketInitialization();
            }
        }catch(Exception e){
            WebServer.logger.err("Catching a "+e.getClass().getName()+": " + e.getMessage());
            if(!WebServer.canIgnoreException(e)){
                e.printStackTrace();
            }
        }finally{
            try{
                clientConnection.close();
                WebServer.logger.debug("Connection with host " + clientIpWithPort + " closed.");
            }catch(IOException e){
                WebServer.logger.err("Cannot close connection.");
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
        HttpRequest request = null;
        ResponseBuilder response = null;

        request = RequestParser.parse(is);
        previousHttpRequest = request;
        if(!requestValidator.requestIsValid(request)){
            request.invalid();
        }
        
        if(request.isValid()){
            response = requestDispatcher.dispatchObjectToHandlers(request);

            // upgrade stuff
            String responseUpgradeHeader = response.getHeaders().getHeader("upgrade");
            String requestUpgradeHeader = request.getHeaders().getHeader("upgrade");
            if(responseUpgradeHeader != null && requestUpgradeHeader != null){
                if(responseUpgradeHeader.equals("websocket") && requestUpgradeHeader.equals("websocket")){
                    currentProtocol = WebProtocol.WEB_SOCKET;
                }
            }else if(sslConnection != null && sslConnection.getApplicationProtocol().equals("h2")){
                currentProtocol = WebProtocol.HTTP_TWO;
            }
        }else{
            response = HttpResponses.generate400Response();
        }

        try(ResponseBuilder res = response){
            String resHeadersString = response.asString();
            os.write(resHeadersString.getBytes());
            WebServer.logger.debug("\n" + resHeadersString.replaceAll("\r\n", "\n"));
            
            if(response.getBody().length() > 0){
                initWriteTimeout(os, WebServer.CONNECTION_WRITE_TIMEOUT_MILSEC);
                // os.write(response.getBody().getBytes());
                response.getBody().streamBytesTo(os);
            }
        }
        
        clientConnection.setSoTimeout(0);
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

    private void initWriteTimeout(OutputStream os, int milsec){
        new Thread("Write Timeout"){
            public void run(){
                try{
                    Thread.sleep(milsec);
                    if(currentProtocol == WebProtocol.HTTP_ORIGINAL)
                        os.close();
                }catch(Exception e){}
                return;
            }
        }.start();
    }
}
