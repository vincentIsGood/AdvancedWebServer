package com.vincentcodes.webserver.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * </p>
 * HttpHandler interface is used to indicate, mostly, classes that
 * has one or more {@link RequestMapping} which are used to handle 
 * {@link HttpRequest}. Technically speaking, 
 * {@link HttpRequestDispatcher} looks for this annotation first.
 * </p>
 * <p>Example:</p>
 * <pre>
 * &#64;HttpHandler
 * public class SomeHandler{
 *     &#64;RequestMapping("/**")
 *     public File handleEverything(HttpRequest req){
 *         ...
 *     }
 *     ...
 * }
 * </pre>
 * @see com.vincentcodes.webserver.annotaion.request.RequestMapping
 * @see com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher
 * @see com.vincentcodes.webserver.component.request.HttpRequest
 * @see com.vincentcodes.webserver.defaults.DefaultHandler
 * @see com.vincentcodes.webserver.defaults.DownloadOnlyHandler
 */
// TYPE - Class, interface (including annotation type), or enum declaration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpHandler {
    
}
