package com.vincentcodes.webserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.vincentcodes.json.CannotMapFromObjectException;
import com.vincentcodes.json.CannotMapToObjectException;
import com.vincentcodes.json.ObjectMapper;
import com.vincentcodes.json.ObjectMapperConfig;
import com.vincentcodes.websocket.WebSocket;
import com.vincentcodes.websocket.WebSocketFrame;
import com.vincentcodes.websocket.constants.OpCode;
import com.vincentcodes.websocket.handler.WebSocketOperator;
import com.vincentcodes.websocket.rpc.JsonRpcRequestObject;
import com.vincentcodes.websocket.rpc.JsonRpcResponseObject;

public class WebSocketConnection {
    private final WebSocket ws;
    private final WebSocketOperator operator;
    private final ObjectMapper mapper;

    public WebSocketConnection(WebSocket ws, Class<? extends WebSocketOperator> eventsHandler)
            throws ReflectiveOperationException {
        this.ws = ws;
        operator = eventsHandler.getConstructor().newInstance();
        mapper = new ObjectMapper(new ObjectMapperConfig.Builder().setAllowMissingProperty(true).build());

        // bi-directional association (throwing terms out cause' I'm still learning)
        ws.setOperator(operator);
        operator.setWebSocket(ws);
    }

    public void init() {
        ws.initConnectionChecker();
        ws.onWebSocketOpen().handleOnOpen();
    }

    /**
     * This is a blocking operation until CLOSE / timeout
     * is reached. For a timeout to occur, checkout
     * {@link WebSocket#initConnectionChecker()}.
     */
    public void start() throws IOException {
        WebSocketFrame frame = null;

        ByteArrayOutputStream payloadOutput = new ByteArrayOutputStream(8192);
        
        try{
            while (true) {
                if((frame = ws.readNextFrame(payloadOutput)).getOpcode() == OpCode.CLOSE){
                    break;
                }

                if (frame.getOpcode() == OpCode.PONG) {
                    ws.pingReceived();
                    continue;
                }
    
                if (frame.getFin() == 0 || frame.getOpcode() == OpCode.CONTINUE) {
                    // keep accumulating data with payloadOutput in readNextFrame()
                    continue;
                }
    
                if (ws.getOperator().isJsonRpcEnabled()) {
                    try {
                        JsonRpcResponseObject res = ws.onJsonRpcReceive()
                                .handle(mapper.jsonToObject(new String(payloadOutput.toByteArray()), JsonRpcRequestObject.class));
                        if (res != null) {
                            ws.send(mapper.objectToJson(res));
                        }
                    } catch (CannotMapToObjectException | CannotMapFromObjectException e) {
                        e.printStackTrace();
                    }
                } else {
                    String res = ws.onMessageReceive().handle(new ByteArrayInputStream(payloadOutput.toByteArray()));
                    if (res != null) {
                        ws.send(res);
                    }
                }
                payloadOutput.reset();
            }
        }finally{
            payloadOutput.close();
            ws.close();
        }
    }

    /**
     * Will not close the underlying socket.
     */
    public void close() {
        ws.onWebSocketClose().handleOnClose();
    }
}
