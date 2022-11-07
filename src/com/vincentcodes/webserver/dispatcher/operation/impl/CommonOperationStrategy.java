package com.vincentcodes.webserver.dispatcher.operation.impl;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.annotaion.request.RequestsFilter;
import com.vincentcodes.webserver.annotaion.wrapper.InvocationCondition;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.HttpResponses;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.IRequestsFilter;
import com.vincentcodes.webserver.dispatcher.operation.MethodInvocationStrategy;
import com.vincentcodes.webserver.dispatcher.operation.OperationResult;
import com.vincentcodes.webserver.dispatcher.operation.OperationResultStatus;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategy;
import com.vincentcodes.webserver.dispatcher.reflect.ConditionalWrapper;
import com.vincentcodes.webserver.dispatcher.reflect.DispatcherEnvironment;
import com.vincentcodes.webserver.reflect.ClassDecorator;
import com.vincentcodes.webserver.reflect.MethodDecorator;
import com.vincentcodes.webserver.reflect.RequestMappingWrapper;

/**
 * Currently, this is the only Strategy. This means this class
 * is used in both {@link HttpDispatcherOperation} and {@link 
 * SimplerHttpDispatcherOperation}. 
 */
public class CommonOperationStrategy implements OperationStrategy<HttpRequest, ResponseBuilder> {

    private final WebServer.Configuration serverConfig;
    private final MethodInvocationStrategy invocationStrategy;

    private List<MethodDecorator> handlers;

    public CommonOperationStrategy(WebServer.Configuration serverConfig, MethodInvocationStrategy invocationStrategy){
        this.serverConfig = serverConfig;
        this.invocationStrategy = invocationStrategy;
    }

    @Override
    public void setHandlers(List<MethodDecorator> handlerMethods){
        this.handlers = handlerMethods;
    }

    @Override
    public OperationResult<ResponseBuilder> execute(HttpRequest request) {
        if(handlers == null){
            throw new IllegalStateException("'handlers' cannot be null in OperationStrategy. Did you use OperationStrategy.setHandlers(List<MethodDecorator>)?");
        }
        try{
            Set<Class<?>> failedFilterCache = new HashSet<>();
            for(MethodDecorator method : handlers){
                if(willFilterOutRequest(request, method, failedFilterCache)){
                    failedFilterCache.add(method.getParent().get());
                    continue;
                }
                if(!doesRequestMethodMatchesMethodAnno(request, method))
                    continue;

                RequestMappingWrapper handlerRequestMapping = getRequestMapping(method);

                // Classes with specified annotations are handled separately
                OperationResult<ResponseBuilder> result = handleClassTypeWithAnno(request, method, handlerRequestMapping);
                if(result != null){
                    if(result.status() == OperationResultStatus.SUCCESS){
                        return result;
                    }
                    // look for methods from other classes
                    continue;
                }

                // For Class which has no requestmapping but methods has one
                if(canInvokeMethod(handlerRequestMapping, method, request)){
                    return OperationResult.success(invocationStrategy.invoke(request, method));
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            return OperationResult.error(HttpResponses.createGenericResponse(500));
        }
        return OperationResult.failure(HttpResponses.generate404Response());
    }
    private boolean doesRequestMethodMatchesMethodAnno(HttpRequest request, MethodDecorator method){
        // If it has no annotation, the method is counted as supporting ALL Http methods
        if(method.hasAnyOneAnnotation(WebServer.SUPPORTED_REQUEST_METHOD.values())){
            // Request method == Method Annotation (eg. @HttpGet)?
            Class<? extends Annotation> reqMethodAnno;
            reqMethodAnno = WebServer.SUPPORTED_REQUEST_METHOD.get(request.getBasicInfo().getMethod());
            if(!method.hasAnnotation(reqMethodAnno)){
                return false;
            }
        }
        return true;
    }
    private RequestMappingWrapper getRequestMapping(MethodDecorator method){
        // If RequestMapping does not exist, meaning that HttpGet and the alike exist
        // RequestMapping value defaults to "/" if this is the case
        RequestMapping reqMappingAnno = method.getAnnotation(RequestMapping.class);
        if(reqMappingAnno != null){
            return new RequestMappingWrapper(reqMappingAnno);
        }
        return new RequestMappingWrapper(RequestMapping.ROOT_PATH);
    }

    /**
     * @return null if class has no RequestMapping annotation.
     */
    private OperationResult<ResponseBuilder> handleClassTypeWithAnno(HttpRequest request, MethodDecorator method, RequestMappingWrapper handlerRequestMapping) throws Exception{
        // Different treatment for class (parent) with RequestMapping annotation
        // For annotation-less methods, they are skipped
        ClassDecorator methodParentClazz = method.getParent();
        if(methodParentClazz.hasAnnotation(RequestMapping.class)){
            String combinedPattern = new RequestMappingWrapper(methodParentClazz.getAnnotation(RequestMapping.class)).combineWith(handlerRequestMapping);
            if(canInvokeMethod(combinedPattern, method, request)){
                return OperationResult.success(invocationStrategy.invoke(request, method));
            }
            return OperationResult.failure(HttpResponses.generate404Response());
        }
        return null;
    }

    private boolean canInvokeMethod(RequestMappingWrapper requestMapping, MethodDecorator method, HttpRequest request) throws ReflectiveOperationException{
        return request.getBasicInfo().getPath().matchesPattern(requestMapping) && allConditionPasses(method, request);
    }
    private boolean canInvokeMethod(String requestMapping, MethodDecorator method, HttpRequest request) throws ReflectiveOperationException{
        return request.getBasicInfo().getPath().matchesPattern(requestMapping) && allConditionPasses(method, request);
    }
    
    /**
     * If method has no InvocationCondition, true is returned
     */
    private boolean allConditionPasses(MethodDecorator method, HttpRequest request) throws ReflectiveOperationException{
        if(!method.hasAnnotation(InvocationCondition.class))
            return true;
        
        DispatcherEnvironment env = new DispatcherEnvironment(serverConfig, request);
        boolean result = true;
        for(Class<? extends ConditionalWrapper> clazz : method.getAnnotation(InvocationCondition.class).value()){
            ConditionalWrapper condition = WebServer.PUBLIC_POOL.getButCreateIfAbsent(clazz);
            result = result && condition.evaluate(env);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private boolean willFilterOutRequest(HttpRequest request, MethodDecorator method, Set<Class<?>> failedFilterCache){
        ClassDecorator parentClass = method.getParent();
        if(failedFilterCache.contains(parentClass.get()))
            return true;
        if(parentClass.hasAnnotation(RequestsFilter.class)){
            RequestsFilter filterAnno = parentClass.getAnnotation(RequestsFilter.class);
            List<Class<? extends IRequestsFilter>> filters = Arrays.stream(filterAnno.value())
                .filter(clazz -> IRequestsFilter.class.isAssignableFrom(clazz))
                .map(clazz -> (Class<? extends IRequestsFilter>)clazz)
                .collect(Collectors.toList());
            return filters.stream()
                .map(clazz -> WebServer.PUBLIC_POOL.getButCreateIfAbsent(clazz))
                .anyMatch(filter -> filter.willFilterOut(request));
        }
        return false;
    }
}
