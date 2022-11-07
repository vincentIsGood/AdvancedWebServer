package com.vincentcodes.websocket.handler;

/**
 * Called immediately before {@link OnMessageReceive}.
 */
public interface OnWebSocketOpen {
    public void handleOnOpen();
}
