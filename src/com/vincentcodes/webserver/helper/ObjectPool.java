package com.vincentcodes.webserver.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.vincentcodes.webserver.util.ConstructorUtils;

public class ObjectPool {
    private final Map<Class<?>, Object> POOL = new HashMap<>();

    /**
     * @return null if not found
     * @see #getInstanceOf
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz){
        return (T) POOL.get(clazz);
    }
    
    /**
     * Get or create
     * @return null if not cannot create an instance
     */
    public <T> T getButCreateIfAbsent(Class<T> clazz){
        @SuppressWarnings("unchecked")
        T result = (T) POOL.get(clazz);
        if(result != null) return result;
        Optional<T> newInstance = ConstructorUtils.noArgNewInstance(clazz);
        if(newInstance.isEmpty())
            return null;
        POOL.put(clazz, newInstance.get());
        return newInstance.get();
    }

    public <T> void putIfNotExist(Class<? extends T> clazz, T instance){
        if(!POOL.containsKey(clazz))
            POOL.put(clazz, instance);
    }
    public <T> void put(Class<? extends T> clazz, T instance){
        POOL.put(clazz, instance);
    }

    public <T> void putIfNotExist(T instance){
        if(!POOL.containsKey(instance.getClass()))
            POOL.put(instance.getClass(), instance);
    }
    public <T> void put(T instance){
        POOL.put(instance.getClass(), instance);
    }

    public boolean hasKey(Class<?> type){
        return POOL.containsKey(type);
    }

    public boolean hasInstanceOf(Class<?> type){
        if(hasKey(type)) return true; // O(1) first
        for(Class<?> clazz : POOL.keySet()){
            if(type.isAssignableFrom(clazz) || type.equals(clazz))
                return true;
        }
        return false;
    }
    
    /**
     * @return null if not found
     */
    public Object getInstanceOf(Class<?> type){
        if(hasKey(type)) return get(type); // O(1) first
        for(Class<?> clazz : POOL.keySet()){
            if(type.isAssignableFrom(clazz) || type.equals(clazz))
                return POOL.get(clazz);
        }
        return null;
    }
}