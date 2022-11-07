package com.vincentcodes.webserver;

import java.lang.reflect.Constructor;

import com.vincentcodes.webserver.exposed.UserConfiguration;
import com.vincentcodes.webserver.helper.ExtendedRegistry;

public class ConfigurationRegister {
    private static ExtendedRegistry<UserConfiguration> REGISTRY = new ExtendedRegistry<>();

    public static void register(Class<? extends UserConfiguration> clazz) throws ReflectiveOperationException{
        Constructor<?> constructor = clazz.getConstructor();
        register((UserConfiguration)constructor.newInstance());
    }

    public static void register(UserConfiguration obj){
        REGISTRY.add(obj);
    }

    public static void clear(){
        REGISTRY.clear();
    }

    public static ExtendedRegistry<UserConfiguration> get(){
        return REGISTRY;
    }

    public static boolean canRegister(Class<?> clazz){
        return UserConfiguration.class.isAssignableFrom(clazz);
    }
}
