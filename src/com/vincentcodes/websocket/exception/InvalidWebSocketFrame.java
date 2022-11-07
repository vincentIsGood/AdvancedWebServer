package com.vincentcodes.websocket.exception;

import java.io.IOException;

public class InvalidWebSocketFrame extends IOException {
    private static final long serialVersionUID = 6813422975112866740L;
    
    public InvalidWebSocketFrame(){
        super();
    }

    public InvalidWebSocketFrame(String message){
        super(message);
    }

    public InvalidWebSocketFrame(String message, Throwable cause){
        super(message, cause);
    }

    public InvalidWebSocketFrame(Throwable cause){
        super(cause);
    }
}
