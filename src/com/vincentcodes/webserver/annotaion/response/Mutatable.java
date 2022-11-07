package com.vincentcodes.webserver.annotaion.response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This specifies that the parameters passed into 
 * a handler are mutatable.
 * 
 * <p>
 * For example, you can mutate {@link 
 * com.vincentcodes.webserver.response.ResponseBuilder
 * ResponseBuilder} to change, for example, its {@link 
 * com.vincentcodes.webserver.response.ResponseBuilder#headers
 * headers} so that the http response can be controlled 
 * by you.
 * </p>
 * 
 * <p>
 * <b>Note</b>:
 * Currently, this annotation only affects the handlers
 * that have {@link 
 * com.vincentcodes.webserver.response.ResponseBuilder
 * ResponseBuilder} as one of its parameters <b>except</b>
 * for handlers with a return type of VOID. For example, 
 * the following code shows mutatable ResponseBuilders
 * </p>
 * 
 * <pre>{@code
 * // VOID as return type: ResponseBuilder in this handler 
 * // is always mutatable
 * public void someHandler(HttpRequest req, ResponseBuilder res){
 *     ...
 * }
 * 
 * &#64;Mutatable
 * public File someHandler(HttpRequest req, ResponseBuilder res){
 *     ...
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mutatable {
    
}
