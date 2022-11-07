package com.vincentcodes.webserver.dispatcher.operation.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import com.vincentcodes.json.CannotMapFromObjectException;
import com.vincentcodes.json.CannotMapToObjectException;
import com.vincentcodes.json.ObjectMapper;
import com.vincentcodes.webserver.annotaion.request.RequestParam;
import com.vincentcodes.webserver.annotaion.response.JsonResponse;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.HttpResponses;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.operation.MethodInvocationStrategy;
import com.vincentcodes.webserver.reflect.MethodDecorator;
import com.vincentcodes.webserver.util.ObjectMappingUtil;

public class SimplerHttpInvocationStrategy implements MethodInvocationStrategy {
    private ObjectMapper objectMapper;

    public SimplerHttpInvocationStrategy(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseBuilder invoke(HttpRequest request, MethodDecorator method)
            throws InvocationTargetException, IOException, IllegalAccessException, IllegalArgumentException {
        try{
            List<Object> parameters = initNullArrayList(method.getParameterCount());
            for(int i = 0; i < method.getParameterCount(); i++){
                Parameter parameter = method.getParameter()[i];
                if(parameter.isAnnotationPresent(RequestParam.class)){
                    RequestParam paramAnnotation = parameter.getAnnotation(RequestParam.class);
                    String paramValue = request.getBasicInfo().getParameter(paramAnnotation.value());
                    if(paramValue == null && !paramAnnotation.nullable())
                        return HttpResponses.generate404Response();
                    
                    if(paramAnnotation.payloadType() == RequestParam.Type.JSON){
                        parameters.set(i, objectMapper.jsonToObject(paramValue, parameter.getType()));
                        continue;
                    }
                    parameters.set(i, ObjectMappingUtil.mapStringToCorrectValue(parameter.getType(), paramValue));
                }
            }
            
            if(method.returnsVoid()){
                return HttpResponses.createGenericResponse(200);
            }
            Object returnValue = method.quickInvoke(parameters.toArray());
            return handleMethodReturnValue(method, returnValue);
        }catch(CannotMapFromObjectException | CannotMapToObjectException objMapperException){
            throw new IllegalArgumentException(objMapperException);
        }
    }
    private ResponseBuilder handleMethodReturnValue(MethodDecorator method, Object returnValue) throws IOException, CannotMapFromObjectException{
        if(returnValue == null){
            return HttpResponses.createGenericResponse(200);
        }
        if(method.hasAnnotation(JsonResponse.class)){
            if(returnValue instanceof String)
                return HttpResponses.useStringAsJsonBody((String)returnValue);
            return HttpResponses.useStringAsJsonBody(objectMapper.objectToJson(returnValue));
        }
        return HttpResponses.useObjectAsBody(returnValue);
    }
    
    private List<Object> initNullArrayList(int capacity){
        List<Object> array = new ArrayList<>(capacity);
        for(int i = 0; i < capacity; i++){
            array.add(null);
        }
        return array;
    }
}
