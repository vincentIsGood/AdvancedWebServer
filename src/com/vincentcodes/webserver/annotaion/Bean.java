package com.vincentcodes.webserver.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vincentcodes.webserver.exposed.BeanDefinitions;

/**
 * Used in {@link BeanDefinitions}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    
}
