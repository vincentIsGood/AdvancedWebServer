package com.vincentcodes.webserver.dispatcher.operation.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.vincentcodes.webserver.HttpHandlerRegister;
import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.HandlerRegistry;
import com.vincentcodes.webserver.dispatcher.operation.DispatcherOperation;
import com.vincentcodes.webserver.dispatcher.operation.OperationResult;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategy;
import com.vincentcodes.webserver.reflect.MethodDecorator;

/**
 * Deals with classes that has &#64;HttpHandler.
 * <p>
 * Start operation is deligated to {@link OperationStrategy}
 */
public class HttpDispatcherOperation extends DispatcherOperation<HttpRequest, ResponseBuilder, MethodDecorator> {
    private OperationStrategy<HttpRequest, ResponseBuilder> operationStrategy;

    /**
     * Operation strategy can be created from {@link OperationStrategyFactory}
     * @param operationStrategy
     */
    public HttpDispatcherOperation(OperationStrategy<HttpRequest, ResponseBuilder> operationStrategy){
        this.operationStrategy = operationStrategy;
        operationStrategy.setHandlers(super.handlers);
    }

    /**
     * Find methods with &#64;RequestMapping, &#64;HttpGet and the alike based on the 
     * objects coming from {@link DispatcherOperation#findHandlerClasses()}
     */
    @Override
    protected List<MethodDecorator> getHandlerMethods(){
        HandlerRegistry filteredRegister = findHandlerClasses();
        List<Class<? extends Annotation>> supportedHandlerAnno = new ArrayList<>(WebServer.SUPPORTED_REQUEST_METHOD.values());
        supportedHandlerAnno.add(RequestMapping.class);
        return filteredRegister.findAllMethodsWithAnyOneOfAnno(supportedHandlerAnno);
    }
    
    @Override
    protected HandlerRegistry findHandlerClasses() {
        HandlerRegistry registeredHandlers = HttpHandlerRegister.getRegistry();
        return registeredHandlers.findAllClassWithAnnotation(HttpHandler.class);
    }

    @Override
    public OperationResult<ResponseBuilder> start(HttpRequest request) {
        return operationStrategy.execute(request);
    }
}
