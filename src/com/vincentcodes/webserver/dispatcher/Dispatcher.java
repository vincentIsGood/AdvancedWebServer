package com.vincentcodes.webserver.dispatcher;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.vincentcodes.webserver.dispatcher.operation.DispatcherOperation;

/**
 * @param T what dispatcher operation takes as its input object
 * @param R what will be returned after handlers are invoked
 * @param M what type of handler methods are they? (eg. MethodDecorator)
 * 
 * @see com.vincentcodes.webserver.dispatcher.operation.DispatcherOperation
 */
public abstract class Dispatcher<T, R, M> {
    protected List<DispatcherOperation<T, R, M>> operations;

    protected Dispatcher(List<DispatcherOperation<T, R, M>> operations){
        this.operations = operations;
    }

    public abstract R dispatchObjectToHandlers(T obj) throws InvocationTargetException;
}
