package com.vincentcodes.webserver.reflect;

import java.lang.annotation.Annotation;

// I can't think of a good name, help.
public class AnnotationWrapper<T extends Annotation> {
    protected T annotation;

    public AnnotationWrapper(T annotation){
        this.annotation = annotation;
    }

    public T get(){
        return annotation;
    }
}
