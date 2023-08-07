package com.vincentcodes.websocket;

import java.io.Closeable;
import java.io.IOException;

import com.vincentcodes.net.UpgradableSocket;
import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.request.HttpRequestPath;
import com.vincentcodes.webserver.helper.IOContainer;
import com.vincentcodes.websocket.constants.OpCode;
import com.vincentcodes.websocket.handler.OnJsonRpcReceive;
import com.vincentcodes.websocket.handler.OnMessageReceive;
import com.vincentcodes.websocket.handler.OnWebSocketClose;
import com.vincentcodes.websocket.handler.OnWebSocketOpen;
import com.vincentcodes.websocket.handler.WebSocketOperator;

/**
 * It doesn't work like a dispatcher because the socket
 * never closes and that there are no HTTP request path
 * once the protocol switching has been completed.
 * 
 * @see https://tools.ietf.org/html/rfc6455
 */
public class WebSocket {
    private final WebSocketFrameGenerator generator;
    private final String path; // eg. "/path/to/ws"
    private final IOContainer ioContainer;
    private WebSocketOperator op;
    private boolean pingSent = false;

    public WebSocket(HttpRequestPath path, IOContainer socket){
        this(path.get(), socket);
    }
    
    public WebSocket(String path, IOContainer socket){
        this.path = path;
        this.ioContainer = socket;
        this.generator = new WebSocketFrameGenerator();
    }

    public void setOperator(WebSocketOperator op){
        this.op = op;
    }
    public WebSocketOperator getOperator(){
        return op;
    }
    
    public OnWebSocketOpen onWebSocketOpen(){
        return op;
    }
    public OnMessageReceive onMessageReceive(){
        return op;
    }
    public OnWebSocketClose onWebSocketClose(){
        return op;
    }
    public OnJsonRpcReceive onJsonRpcReceive(){
        return op;
    }

    /**
     * @return path of this ws
     */
    public String getIdentifier() {
        return path;
    }

    /**
     * This is a blocking method. It waits for the client to
     * send data through the stream and tries to parse it into 
     * a WebSocketFrame.
     */
    public WebSocketFrame readNextFrame() throws IOException{
        WebSocketFrame frame = WebSocketFrameParser.parse(ioContainer.getInputStream());

        if(WebServer.lowLevelDebugMode)
            WebServer.logger.debug("Ws Recv: " + frame.toString());

        return frame;
    }

    /**
     * This is the default send method. It will not handle large 
     * payloads by default. Send a continue frame if you want 
     * any fragmentation of data.
     * @see WebSocketFrameGenerator#basicCloseFrame(OpCode, String)
     */
    // TODO: handle large payloads (consider fragmentation? ie. make use of continue frames)
    public void send(String payload) throws IOException{
        send(generator.basicCloseFrame(OpCode.TEXT, payload));
    }

    /**
     * @see WebSocketFrameGenerator#basicContinueFrame(OpCode, String)
     */
    public void sendContinue(String payload) throws IOException{
        send(generator.basicContinueFrame(OpCode.TEXT, payload));
    }

    public void send(WebSocketFrame frame) throws IOException{
        if(WebServer.lowLevelDebugMode)
            WebServer.logger.debug("Ws Send: " + frame.toString());

        ioContainer.getOutputStream().write(frame.toBytes());
    }

    /**
     * Closes the underlying socket. Be very careful.
     */
    public void close() throws IOException{
        Closeable socket = ioContainer.getSocket();
        socket.close();
    }

    /**
     * Use this method in conjunction with {@link #pingReceived()}
     * like this:
     * <pre>
     * if(frame.getOpcode() == OpCode.PONG){
     *    ws.pingReceived();
     *    ...
     * }
     * </pre>
     */
    public void initConnectionChecker(){
        Thread connectionChecker = new Thread("WebSocket Connection Checker"){
            public void run(){
                try{
                    UpgradableSocket socket = ioContainer.getSocket();
                    while(!socket.isClosed()){
                        if(pingSent) socket.close();
                        sendPing();
                        Thread.sleep(WebServer.WEBSOCKET_PING_INTERVAL_MILSEC);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                return;
            }
        };
        connectionChecker.setDaemon(true);
        connectionChecker.start();
    }

    public void sendPing() throws IOException{
        send(generator.pingFrame());
        pingSent = true;
    }

    public void pingReceived(){
        pingSent = false;
    }
}
