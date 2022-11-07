package com.vincentcodes.webserver.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vincentcodes.webserver.WebServer;

/**
 * It works on fields with {@link WebServer} type only. 
 * {@code public} modifier must be used.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoInjected {
    
}
