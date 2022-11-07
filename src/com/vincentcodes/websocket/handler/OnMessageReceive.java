package com.vincentcodes.websocket.handler;

import com.vincentcodes.webserver.ServerThread;

/**
 * If the return value is null, then a frame will
 * not be sent. 
 * @see ServerThread#websocketInitialization
 */
public interface OnMessageReceive {
    public String handle(String payload);
}
