package com.vincentcodes.webserver.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Definition: A book or record containing a list of "things"
 */
public class Registry<T> {
    protected List<T> register;

    public Registry(){
        register = new ArrayList<>();
    }

    public Registry(List<T> initValues){
        register = new ArrayList<>(initValues);
    }

    /**
     * add an object to the register
     * @return whether the operation is successful or not
     */
    public boolean add(T obj){
        return register.add(obj);
    }

    public boolean add(List<T> objs){
        return register.addAll(objs);
    }

    public List<T> get(){
        return register;
    }

    public void clear(){
        register.clear();
    }

    public int size(){
        return register.size();
    }

    public T[] toArray(T[] emptyArray){
        return register.toArray(emptyArray);
    }

    public List<T> readOnly(){
        return Collections.unmodifiableList(register);
    }
}
