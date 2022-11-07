package com.vincentcodes.webserver.reflect;

import java.lang.reflect.Field;

public class FieldDecorator {
    private Object owner;
    private Field field;

    public FieldDecorator(Object owner, Field field) {
        this.owner = owner;
        this.field = field;
    }

    public Object getOwner() {
        return owner;
    }

    public Field get() {
        return field;
    }

    public void setAccessible(boolean flag){
        field.setAccessible(flag);
    }

    public boolean hasValue() throws IllegalArgumentException, IllegalAccessException{
        return field.get(owner) != null;
    }

    public Object getValue() throws IllegalArgumentException, IllegalAccessException{
        return field.get(owner);
    }

    public void setValue(Object value) throws IllegalArgumentException, IllegalAccessException {
        field.set(owner, value);
    }

    public Class<?> type(){
        return field.getType();
    }
}
