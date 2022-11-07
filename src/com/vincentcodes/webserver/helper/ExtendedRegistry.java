package com.vincentcodes.webserver.helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.vincentcodes.webserver.reflect.FieldDecorator;
import com.vincentcodes.webserver.reflect.MethodDecorator;

/**
 * A registry which gives access to finding annotations from registered objects  
 */
public class ExtendedRegistry<T> extends Registry<T>{
    public ExtendedRegistry(){
        super();
    }

    public ExtendedRegistry(List<T> initValues){
        super(initValues);
    }

    @Override
    public boolean add(T obj){
        return register.add(obj);
    }

    public T findObjectWithClassName(String name){
        // maybe use set instead for better performance?
        for(T obj : register){
            if(obj.getClass().getSimpleName().equals(name))
                return obj;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <R> ExtendedRegistry<R> findAllClassAssignableTo(Class<R> superClass){
        return new ExtendedRegistry<R>(register.stream().filter(obj -> superClass.isAssignableFrom(obj.getClass()))
            .map(obj -> (R)obj)
            .collect(Collectors.toList()));
    }

    public ExtendedRegistry<T> findAllClassWithAnnotation(Class<? extends Annotation> annotation){
        return new ExtendedRegistry<T>(register.stream().filter(obj -> obj.getClass().isAnnotationPresent(annotation)).collect(Collectors.toList()));
    }

    // maybe parallel stream will be better? (not necessarily faster)
    public List<MethodDecorator> findAllMethodsWithAnnotation(Class<? extends Annotation> annotation){
        ArrayList<MethodDecorator> methods = new ArrayList<>();
        for(T obj : register){
            for (Method meth : obj.getClass().getDeclaredMethods()) {
                if (meth.isAnnotationPresent(annotation))
                    methods.add(new MethodDecorator(obj, meth));
            }
        }
        return methods;
    }
    
    public List<MethodDecorator> findAllMethodsWithAnyOneOfAnno(List<Class<? extends Annotation>> annotations){
        ArrayList<MethodDecorator> methods = new ArrayList<>();
        for(T obj : register){
            for (Method meth : obj.getClass().getDeclaredMethods()) {
                for(Class<? extends Annotation> annotation : annotations){
                    if (meth.isAnnotationPresent(annotation))
                        methods.add(new MethodDecorator(obj, meth));
                }
            }
        }
        return methods;
    }

    public MethodDecorator findFirstMethodWithAnnotation(Class<? extends Annotation> annotation){
        for(T obj : register){
            for (Method meth : obj.getClass().getDeclaredMethods()) {
                if (meth.isAnnotationPresent(annotation))
                    return new MethodDecorator(obj, meth);
            }
        }
        return null;
    }

    public List<FieldDecorator> findAllFieldsWithAnnotation(Class<? extends Annotation> annotation){
        ArrayList<FieldDecorator> fields = new ArrayList<>();
        for(T obj : register){
            for(Field field : obj.getClass().getDeclaredFields()){
                if(field.isAnnotationPresent(annotation))
                    fields.add(new FieldDecorator(obj, field));
            }
        }
        return fields;
    }
}
