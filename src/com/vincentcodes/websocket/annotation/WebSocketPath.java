package com.vincentcodes.websocket.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to identify which handler a websocket session 
 * should connect to (uses a path as identifier; eg. 
 * value = "/path/to/ws") 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketPath {
    String value();
}