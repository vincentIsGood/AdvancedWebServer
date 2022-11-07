package com.vincentcodes.webserver;

import java.lang.reflect.Constructor;

import com.vincentcodes.webserver.helper.ExtendedRegistry;
import com.vincentcodes.websocket.handler.OnMessageReceive;
import com.vincentcodes.websocket.handler.WebSocketOperator;

public class WebSocketHandlerRegister {
    private static ExtendedRegistry<WebSocketOperator> REGISTRY = new ExtendedRegistry<>();

    public static void register(Class<? extends WebSocketOperator> clazz) throws ReflectiveOperationException{
        Constructor<?> constructor = clazz.getConstructor();
        register((WebSocketOperator)constructor.newInstance());
    }

    public static void register(WebSocketOperator obj){
        REGISTRY.add(obj);
    }

    public static void clear(){
        REGISTRY.clear();
    }

    public static ExtendedRegistry<WebSocketOperator> get(){
        return REGISTRY;
    }

    public static boolean canRegister(Class<?> clazz){
        return OnMessageReceive.class.isAssignableFrom(clazz);
    }
}
