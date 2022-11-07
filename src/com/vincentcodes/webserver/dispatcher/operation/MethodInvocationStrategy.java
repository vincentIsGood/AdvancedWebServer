package com.vincentcodes.webserver.dispatcher.operation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.operation.impl.HttpDispatcherOperation;
import com.vincentcodes.webserver.reflect.MethodDecorator;

/**
 * Replace Method with Method Object. This class is used in 
 * {@link HttpDispatcherOperation}.
 * @see com.vincentcodes.webserver.dispatcher.operation.impl.HttpDispatcherOperation
 */
public interface MethodInvocationStrategy {
    public ResponseBuilder invoke(HttpRequest request, MethodDecorator method) 
            throws InvocationTargetException, IOException, IllegalAccessException, IllegalArgumentException;
}
