package com.vincentcodes.webserver.dispatcher.operation;

import java.util.List;

import com.vincentcodes.webserver.helper.Registry;
import com.vincentcodes.webserver.reflect.MethodDecorator;

/**
 * <p>
 * This class is used to inside a dispatcher.
 * </p>
 * 
 * @param T what dispatcher operation takes as its input object
 * @param R what will be returned after the operation is done
 * @param M what type of handler methods are they? Often, it's 
 * a {@link java.lang.reflect.Method Method}, {@link MethodDecorator},
 * or custom made methods.
 * 
 * @see com.vincentcodes.webserver.dispatcher.Dispatcher
 * @see https://dzone.com/articles/java-reflection-but-faster#
 */
public abstract class DispatcherOperation<T, R, M> {
    /**
     * Methods which have &#64;RequestMapping. It is set based on the 
     * return value of {@link DispatcherOperation#getHandlerMethods()}
     */
    protected List<M> handlers;

    public DispatcherOperation(){
        this.handlers = getHandlerMethods();
    }

    /**
     * May make use of {@link DispatcherOperation#findHandlerClasses()}
     */
    protected abstract List<M> getHandlerMethods();

    /**
     * Define what handlers this operation will act on.
     * @return (eg. Objects (Classes) containing the specified annotation)
     */
    protected abstract Registry<?> findHandlerClasses();

    /**
     * This method will start an operation which will be defined by its sub-classes
     */
    public abstract OperationResult<R> start(T input);
}
