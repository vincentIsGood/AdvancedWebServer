package com.vincentcodes.websocket;

import java.util.Optional;

import com.vincentcodes.webserver.WebSocketHandlerRegister;
import com.vincentcodes.webserver.helper.ExtendedRegistry;
import com.vincentcodes.websocket.annotation.WebSocketPath;
import com.vincentcodes.websocket.handler.WebSocketOperator;

/**
 * This class makes use of {@link WebSocketHandlerRegister}
 */
public class WebSocketHandlerSelector {
    private ExtendedRegistry<WebSocketOperator> handlers;

    public WebSocketHandlerSelector(){
        this.handlers = WebSocketHandlerRegister.get().findAllClassWithAnnotation(WebSocketPath.class);
    }

    /**
     * Search for handler based on the path of ws and operator
     */
    public Optional<WebSocketOperator> searchForHandler(WebSocket webSocket){
        for(WebSocketOperator handler : handlers.get()){
            WebSocketPath path = handler.getClass().getAnnotation(WebSocketPath.class);
            if(path.value().equals(webSocket.getIdentifier())){
                return Optional.of(handler);
            }
        }
        return Optional.empty();
    }
}
