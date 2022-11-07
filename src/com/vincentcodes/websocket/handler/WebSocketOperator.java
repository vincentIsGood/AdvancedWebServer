package com.vincentcodes.websocket.handler;

import com.vincentcodes.websocket.WebSocket;

/**
 * Please use empty parameter constructor for child 
 * classes.
 * <p>
 * You can enable JsonRpc by using `enableJsonRpc()` 
 * in your constructor.
 */
public abstract class WebSocketOperator implements OnWebSocketOpen, OnMessageReceive, OnWebSocketClose, OnJsonRpcReceive{
    private WebSocket ws;
    
    /**
     * If this is set, OnMessageReceive will not be invoked
     */
    private boolean enableJsonRpc = false;

    /**
     * Will be set by the ServerThread during runtime.
     */
    public void setWebSocket(WebSocket ws){
        this.ws = ws;
    }
    public WebSocket getWebSocket(){
        return ws;
    }

    public boolean isJsonRpcEnabled(){
        return enableJsonRpc;
    }
    public void enableJsonRpc(){
        enableJsonRpc = true;
    }
    public void disableJsonRpc(){
        enableJsonRpc = false;
    }
}
