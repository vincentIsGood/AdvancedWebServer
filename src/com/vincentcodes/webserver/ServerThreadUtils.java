package com.vincentcodes.webserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.HttpRequestValidator;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.exception.CannotParseRequestException;
import com.vincentcodes.webserver.helper.HttpTunnel;
import com.vincentcodes.webserver.helper.IOContainer;
import com.vincentcodes.webserver.http2.Http2Connection;
import com.vincentcodes.websocket.WebSocket;
import com.vincentcodes.websocket.WebSocketHandlerSelector;
import com.vincentcodes.websocket.handler.WebSocketOperator;

public class ServerThreadUtils {

    /**
     * Still in experimental phase
     */
    public static void http2Initialization(IOContainer socket, HttpRequestValidator requestValidator, HttpRequestDispatcher requestDispatcher) throws IOException, CannotParseRequestException, InvocationTargetException{
        Http2Connection http2Connection = new Http2Connection(socket, requestValidator, requestDispatcher);
        http2Connection.setup();
        http2Connection.takeover();
    }


    /**
     * WebSocket is supported. But it still needs more testings
     */
    public static void websocketInitialization(HttpRequest request, IOContainer socket) throws IOException, ReflectiveOperationException{
        WebSocketHandlerSelector selector = new WebSocketHandlerSelector();
        WebSocket ws = new WebSocket(request.getBasicInfo().getPath(), socket);

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
     * This is a blocking method.
     * 
     * Note: http2 don't seem to work because my webserver has done the initial part 
     * (ie. settingsframe) causing an inconsistency between client and remote server.
     * 
     * @param request client request
     * @param response Response of this webserver corresponding to the client request. 
     * It checks if any handlers allows the use of tunneling
     * @param httpReplyClient this consumer is only used whenever request exchange happens.
     * It takes remote server response as the 1st arg and output stream of the current 
     * webserver's socket as the 2nd arg.
     */
    public static void socketTunnelInitialization(HttpRequest request, ResponseBuilder response, BiConsumer<ResponseBuilder, OutputStream> httpReplyClient) throws IOException, InterruptedException{
        // TODO: sanitize destination?
        String tunnelDest = response.getHeaders().getHeader("X-Vws-Raw-Tunnel");
        // what to do with the current request
        String requestPolicy = response.getHeaders().getHeader("X-Vws-Req-Policy").toLowerCase();

        int colonIndex = 0;
        int port = 80;
        if((colonIndex = tunnelDest.lastIndexOf(':')) != -1){
            port = Integer.parseInt(tunnelDest.substring(colonIndex+1));
            tunnelDest = tunnelDest.substring(0, colonIndex);
        }

        Thread remoteServerReaderThread = null;
        boolean useSsl = response.getHeaders().hasHeader("X-Vws-Ssl-Tunnel")? Boolean.parseBoolean(response.getHeaders().getHeader("X-Vws-Ssl-Tunnel")) : false;
        boolean dangerous = response.getHeaders().hasHeader("X-Vws-Ssl-Tunnel-Danger")? Boolean.parseBoolean(response.getHeaders().getHeader("X-Vws-Ssl-Tunnel-Danger")) : false;
        try(HttpTunnel tunnel = new HttpTunnel(tunnelDest, port, useSsl, dangerous)){
            tunnel.open();
            IOContainer socket = request.getSocket();
            BufferedInputStream tunnelIs = new BufferedInputStream(tunnel.getSocket().getInputStream());
            BufferedOutputStream tunnelOs = new BufferedOutputStream(tunnel.getSocket().getOutputStream());
            
            if(requestPolicy.equals("exchange")){
                // Just reply client, without modifying their response.
                httpReplyClient.accept(tunnel.send(request), socket.getOutputStream());
            }else if(requestPolicy.equals("one-way")){
                tunnel.sendOneWay(request);
            }

            (remoteServerReaderThread = new Thread("Tunnel: Remote Server Reader Thread"){
                @Override
                public void run() {
                    try {
                        HttpTunnel.streamToUntilClose(tunnelIs, socket.getOutputStream());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }).start();

            HttpTunnel.streamToUntilClose(socket.getInputStream(), tunnelOs);
        }finally{
            if(remoteServerReaderThread != null){
                remoteServerReaderThread.interrupt();
                remoteServerReaderThread.join();
            }
        }
    }
}
