package com.vincentcodes.webserver.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class ConstructorUtils {
    public static <T> Optional<T> noArgNewInstance(Class<? extends T> clazz){
        try {
            Constructor<? extends T> constructor = clazz.getConstructor();
            return Optional.of(constructor.newInstance());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
