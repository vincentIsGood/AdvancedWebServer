package com.vincentcodes.webserver.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate a class which will
 * <b>NEVER</b> contain annotated handlers that take 
 * {@link com.vincentcodes.webserver.component.request.HttpRequest
 * HttpRequest} or {@link com.vincentcodes.webserver.component.response.ResponseBuilder 
 * ResponseBuilder} as one of its method arguments. Instead, this annotaion
 * can be used to focus more on properly handling client's
 * API requests. An example is illustrated down below...
 * 
 * <p>Example:</p>
 * <pre>
 * &#64;SimplerHttpHandler
 * public class SomeHandler{
 *     &#64;HttpGet
 *     &#64;RequestMapping("/**")
 *     public String handleEverything(@RequestParam("id") int id){
 *         ...
 *     }
 *     ...
 * }
 * </pre>
 * 
 * @see com.vincentcodes.webserver.component.request.HttpRequest
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimplerHttpHandler {
    
}
