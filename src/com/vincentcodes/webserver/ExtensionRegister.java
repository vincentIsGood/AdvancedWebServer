package com.vincentcodes.webserver;

import java.util.List;

import com.vincentcodes.webserver.exposed.UserConfiguration;
import com.vincentcodes.websocket.handler.WebSocketOperator;

/**
 * This is an adapter.
 */
public class ExtensionRegister {
    public static void register(List<Class<?>> classes){
        classes.forEach(ExtensionRegister::register);
    }

    @SuppressWarnings("unchecked")
    public static void register(Class<?> clazz){
        try{
            // See if it implements interface OnMessageReceive
            if(WebSocketHandlerRegister.canRegister(clazz)){
                WebSocketHandlerRegister.register((Class<? extends WebSocketOperator>)clazz);
            }else if(HttpHandlerRegister.canRegister(clazz)){
                HttpHandlerRegister.register(clazz);
            }else if(ConfigurationRegister.canRegister(clazz)){
                ConfigurationRegister.register((Class<? extends UserConfiguration>)clazz);
            }else{
                return;
            }
            WebServer.logger.info("Class '" + clazz.getName() + "' is registered");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void register(Object obj){
        register(obj.getClass());
    }

    public static void clear(){
        HttpHandlerRegister.clear();
        WebSocketHandlerRegister.clear();
    }
}
