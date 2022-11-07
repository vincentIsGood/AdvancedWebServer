package com.vincentcodes.webserver.dispatcher.operation;

import java.util.List;

import com.vincentcodes.webserver.reflect.MethodDecorator;

/**
 * {@link MethodDecorator} are used to wrap handlers. Currently,
 * {@link OperationStrategy} should be used in {@link DispatcherOperation}.
 * {@link DispatcherOperation} will search for handlers while
 * {@link OperationStrategy} is used to determine what will be
 * done to the handlers. Since same {@link DispatcherOperation} 
 * may have different ways of dealing with handlers, {@link OperationStrategy}
 * is created to enable that flexibility.
 * 
 * @param T what dispatcher operation takes as its input object
 * @param R what will be returned after the operation is done
 */
public interface OperationStrategy<T, R> {
    public void setHandlers(List<MethodDecorator> handlerMethods);

    public OperationResult<R> execute(T input);
}
