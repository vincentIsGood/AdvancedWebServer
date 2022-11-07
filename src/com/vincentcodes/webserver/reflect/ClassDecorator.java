package com.vincentcodes.webserver.reflect;

import java.lang.annotation.Annotation;

public class ClassDecorator {
    private Object object;
    private Class<?> clazz;

    public ClassDecorator(Object object, Class<?> clazz){
        this.object = object;
        this.clazz = clazz;
    }

    public Object getOwner(){
        return object;
    }

    public Class<?> get(){
        return clazz;
    }

    /**
     * All of the annotations must exist inside this method to return true
     */
    public boolean hasAnnotations(Iterable<Class<? extends Annotation>> annotations){
        boolean allExists = true;
        for(Class<? extends Annotation> annotation : annotations){
            if(!hasAnnotation(annotation)){
                return !allExists;
            }
        }
        return allExists;
    }

    /**
     * !hasAnyOneAnnotation -> None
     */
    public boolean hasAnyOneAnnotation(Iterable<Class<? extends Annotation>> annotations){
        for(Class<? extends Annotation> annotation : annotations){
            if(hasAnnotation(annotation)){
                return true;
            }
        }
        return false;
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotation){
        return clazz.isAnnotationPresent(annotation);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotation){
        if(hasAnnotation(annotation))
            return clazz.getAnnotation(annotation);
        return null;
    }
}
