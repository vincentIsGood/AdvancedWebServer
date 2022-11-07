package com.vincentcodes.webserver.annotaion.request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vincentcodes.webserver.dispatcher.IRequestsFilter;

/**
 * Used on classes annotated with HttpHandler. Classes must 
 * implement {@link IRequestsFilter} interface.
 * 
 * Filters out request when any of the filters returns true.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestsFilter {
    Class<?>[] value();
}
