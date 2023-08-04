package com.vincentcodes.webserver;

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
     * @param request client request
     * @param response Response of this webserver corresponding to the client request. 
     * It checks if any handlers allows the use of tunneling
     * @param httpReplyClient this consumer is only used whenever request exchange happens. 
     * It takes remote server response as the 1st arg and output stream of the current 
     * webserver's socket as the 2nd arg.
     */
    public static void socketTunnelInitialization(HttpRequest request, ResponseBuilder response, BiConsumer<ResponseBuilder, OutputStream> httpReplyClient) throws IOException, InterruptedException{
        String tunnelDest = response.getHeaders().getHeader("X-Vws-Raw-Tunnel");
        boolean exchangeRequest = Boolean.parseBoolean(response.getHeaders().getHeader("X-Vws-Exchange-Once"));
        // TODO: sanitize dest

        int colonIndex = 0;
        int port = 80;
        if((colonIndex = tunnelDest.lastIndexOf(':')) != -1){
            port = Integer.parseInt(tunnelDest.substring(colonIndex+1));
            tunnelDest = tunnelDest.substring(0, colonIndex);
        }

        Thread wsReaderThread = null;
        System.out.println("starting tunnel to: " + tunnelDest + " " + port);
        try(HttpTunnel tunnel = new HttpTunnel(tunnelDest, port)){
            IOContainer socket = request.getSocket();
            tunnel.open();

            if(exchangeRequest){
                // Just reply client, without modifying their response.
                httpReplyClient.accept(tunnel.send(request), socket.getOutputStream());
            }

            wsReaderThread = new Thread(){
                @Override
                public void run() {
                    try {
                        HttpTunnel.streamToUntilClose(tunnel.getSocket().getInputStream(), socket.getOutputStream());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            };
            wsReaderThread.start();
            HttpTunnel.streamToUntilClose(socket.getInputStream(), tunnel.getSocket().getOutputStream());
        }finally{
            if(wsReaderThread != null){
                wsReaderThread.interrupt();
                wsReaderThread.join();
            }
        }
    }
}
