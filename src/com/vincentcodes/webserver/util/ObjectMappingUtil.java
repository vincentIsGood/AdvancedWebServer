package com.vincentcodes.webserver.util;

public class ObjectMappingUtil {
    public static Object mapStringToCorrectValue(Class<?> clazz, String value){
        // primitives
        if(clazz.equals(int.class) || clazz.equals(Integer.class)){
            if(value == null) return -1;
            return Integer.parseInt(value);
        }else if(clazz.equals(double.class) || clazz.equals(Double.class)){
            if(value == null) return -1.0;
            return Double.parseDouble(value);
        }else if(clazz.equals(byte.class) || clazz.equals(Byte.class)){
            if(value == null) return (byte)-1;
            return Byte.parseByte(value);
        }else if(clazz.equals(short.class) || clazz.equals(Short.class)){
            if(value == null) return (short)-1;
            return Short.parseShort(value);
        }else if(clazz.equals(long.class) || clazz.equals(Long.class)){
            if(value == null) return (long)-1;
            return Long.parseLong(value);
        }else if(clazz.equals(float.class) || clazz.equals(Float.class)){
            if(value == null) return (float)-1;
            return Float.parseFloat(value);
        }else if(clazz.equals(boolean.class) || clazz.equals(Boolean.class)){
            if(value == null) return false;
            return Boolean.parseBoolean(value);
        }else if(clazz.equals(char.class) || clazz.equals(Character.class)){
            if(value == null) return null;
            return value.charAt(0);
        }
        return value;
    }
}
