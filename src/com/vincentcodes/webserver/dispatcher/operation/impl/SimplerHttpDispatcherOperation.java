package com.vincentcodes.webserver.dispatcher.operation.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.vincentcodes.webserver.HttpHandlerRegister;
import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.annotaion.SimplerHttpHandler;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.HandlerRegistry;
import com.vincentcodes.webserver.dispatcher.operation.DispatcherOperation;
import com.vincentcodes.webserver.dispatcher.operation.OperationResult;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategy;
import com.vincentcodes.webserver.reflect.MethodDecorator;

/**
 * <p>
 * Deals with classes that have {@link SimplerHttpHandler &#64;SimplerHttpHandler}
 * <p>
 * The methods / handlers inside &#64;SimplerHttpHandler class is similar to 
 * &#64;HttpHandler in that the annotations like HttpGet, RequestMapping,
 * AutoInjected and so on can be used (except for Mutatable). The only 
 * difference is that simpler handlers will never deal with the webserver 
 * stuff (eg. HttpRequest and ResponseBuilder).
 * <p>
 * Note that the ObjectMapper requires classes to be annotated with 
 * &#64;JsonSerializable if you want to escape this constraint, you may need
 * to use a new ObjectMapper inside your class and return String for your 
 * method
 * 
 * <p>
 * Start operation is deligated to {@link OperationStrategy}
 */
public class SimplerHttpDispatcherOperation extends DispatcherOperation<HttpRequest, ResponseBuilder, MethodDecorator> {
    
    private OperationStrategy<HttpRequest, ResponseBuilder> operationStrategy;

    public SimplerHttpDispatcherOperation(OperationStrategy<HttpRequest, ResponseBuilder> operationStrategy){
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
        HandlerRegistry register = HttpHandlerRegister.getRegistry();
        return register.findAllClassWithAnnotation(SimplerHttpHandler.class);
    }

    @Override
    public OperationResult<ResponseBuilder> start(HttpRequest request) {
        return operationStrategy.execute(request);
    }
}
