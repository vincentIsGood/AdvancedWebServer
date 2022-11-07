package com.vincentcodes.webserver.dispatcher;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import com.vincentcodes.webserver.helper.ExtendedRegistry;

/**
 * A registry which gives access to finding annotations from registered objects
 */
public class HandlerRegistry extends ExtendedRegistry<Object>{
    public HandlerRegistry(){
        super();
    }

    public HandlerRegistry(List<Object> initValues){
        super(initValues);
    }

    public HandlerRegistry findAllClassWithAnnotation(Class<? extends Annotation> annotation){
        return new HandlerRegistry(register.stream().filter(obj -> obj.getClass().isAnnotationPresent(annotation)).collect(Collectors.toList()));
    }
}
