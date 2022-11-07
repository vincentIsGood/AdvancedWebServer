package com.vincentcodes.webserver;

import java.lang.reflect.Constructor;

import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.SimplerHttpHandler;
import com.vincentcodes.webserver.defaults.DefaultHandler;
import com.vincentcodes.webserver.dispatcher.HandlerRegistry;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;

/**
 * A register used to store HttpHandler / SimplerHttpHandler Classes (more technically, 
 * it stores &#64;HttpHandler / &#64;SimplerHttpHandler annotated classes)
 * <p>
 * The class may or may not contain {@link com.vincentcodes.webserver.annotaion.request.RequestMapping
 * &#64;RequestMapping} public methods which are used to process 
 * one or more {@link com.vincentcodes.webserver.component.request.HttpRequest 
 * HttpRequests} by {@link HttpRequestDispatcher}.
 */
public class HttpHandlerRegister {
    private static final HandlerRegistry REGISTRY = new HandlerRegistry();

    private static boolean add(Object obj){
        if(canRegister(obj.getClass())){
            return REGISTRY.add(obj);
        }else{
            return false;
        }
    }

    public static void register(Object handler){
        if(!add(handler)){
            WebServer.logger.err("Class '"+ handler.getClass().getName() +"' cannot be registered.");
        }
    }

    /**
     * Attempts to create an instance from clazz and register it
     */
    public static void register(Class<?> clazz) throws ReflectiveOperationException{
        try{
            Constructor<?> constructor = clazz.getConstructor();
            register(constructor.newInstance());
        }catch(ReflectiveOperationException e){
            throw new ReflectiveOperationException("Unable to create an instance of class: " + clazz.getName(), e);
        }
    }

    /**
     * This method will remove {@link DefaultHandler}. This often happens
     * when you want to create a custom web server that does stuff differently.
     * For example, you may want to use this project's jar as API which allows
     * you to {@link HttpHandlerRegister#register(Object)} a custom handler.
     */
    public static void clear(){
        REGISTRY.clear();
    }

    /**
     * Get registered classes
     */
    public static HandlerRegistry getRegistry(){
        return REGISTRY;
    }

    public static boolean canRegister(Class<?> clazz){
        return clazz.isAnnotationPresent(HttpHandler.class)
        || clazz.isAnnotationPresent(SimplerHttpHandler.class);
    }
}
