package com.vincentcodes.webserver;

import java.util.List;
import java.util.stream.Collectors;

import com.vincentcodes.webserver.annotaion.Bean;
import com.vincentcodes.webserver.exposed.BeanDefinitions;
import com.vincentcodes.webserver.helper.ExtendedRegistry;
import com.vincentcodes.webserver.helper.ObjectPool;
import com.vincentcodes.webserver.reflect.MethodDecorator;

public class BeanInitializer {
    private ExtendedRegistry<BeanDefinitions> defClasses;
    private List<Object> initializedObjects;
    
    public BeanInitializer(ExtendedRegistry<BeanDefinitions> defClasses){
        this.defClasses = defClasses;
    }

    // TODO: Currently, bean cannot take any parameters
    // TODO: Cannot choose between multiple same type of classes (use name to distinguish them)
    /**
     * Find beans from BeanDefinitions provided from the Constructor
     */
    public BeanInitializer start(){
        try{
            initializedObjects = defClasses.findAllMethodsWithAnnotation(Bean.class).stream()
                .filter(method -> !method.returnsVoid())
                .map(obj -> createObjectFromMethod(obj))
                .collect(Collectors.toList());
            return this;
        }catch(Exception beanInitializationException){
            throw new RuntimeException("Cannot initialize bean. ", beanInitializationException);
        }
    }

    public void addToPool(ObjectPool pool){
        initializedObjects.forEach(obj -> pool.put(obj.getClass(), obj));
    }

    private Object createObjectFromMethod(MethodDecorator obj) {
        try{
            return obj.invoke();
        }catch(ReflectiveOperationException e){
            throw new RuntimeException("Cannot initialize bean. ", e);
        }
    }
}
