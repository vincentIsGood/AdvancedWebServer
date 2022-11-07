package com.vincentcodes.webserver.annotaion.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vincentcodes.webserver.dispatcher.reflect.ConditionalWrapper;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeInvocation {
    Class<? extends ConditionalWrapper>[] value();
}
