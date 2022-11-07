package com.vincentcodes.webserver.dispatcher.operation;

import com.vincentcodes.json.ObjectMapper;
import com.vincentcodes.json.ObjectMapperConfig;
import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.operation.impl.CommonOperationStrategy;
import com.vincentcodes.webserver.dispatcher.operation.impl.HttpInvocationStrategy;
import com.vincentcodes.webserver.dispatcher.operation.impl.SimplerHttpInvocationStrategy;

/**
 * Operation strategy creates a strategy to deal with
 * handler methods. Currently we have one implementation
 * only, namely, {@link CommonOperationStrategy}.
 */
public class OperationStrategyFactory {
    public static enum InvocationTypes{
        DEFAULT,
        NORMAL_HTTP,
        SIMPLER_HTTP
    }

    private final WebServer.Configuration serverConfig;
    
    public OperationStrategyFactory(WebServer.Configuration serverConfig){
        this.serverConfig = serverConfig;
    }

    /**
     * @param invocationType used to specify what {@link MethodInvocationStrategy}
     * is going to be used. 
     * @return a partially configured {@link OperationStrategy} at your disposal.
     * Remember to set handlers to the strategy from preferably {@link DispatcherOperation}.
     */
    public OperationStrategy<HttpRequest, ResponseBuilder> create(OperationStrategyFactory.InvocationTypes type){
        MethodInvocationStrategy invocationStrategy;
        if(type == InvocationTypes.DEFAULT || type == InvocationTypes.NORMAL_HTTP){
            invocationStrategy = new HttpInvocationStrategy();
            
        }else if(type == InvocationTypes.SIMPLER_HTTP){
            ObjectMapper objectMapper = new ObjectMapper(new ObjectMapperConfig.Builder()
                .setAllowMissingProperty(true)
                .setSerializableAnnotationRequired(true)
                .build());
            invocationStrategy = new SimplerHttpInvocationStrategy(objectMapper);
        }else{
            throw new IllegalArgumentException("Do not support invocation type of '" + type.toString() + "'"); 
        }
        return new CommonOperationStrategy(serverConfig, invocationStrategy);
    }
}
