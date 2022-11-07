package com.vincentcodes.webserver.annotaion.request;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This parameter is used to indicate what parameter
 * you want to get from a URL. For example, if http:
 * //somedomain.com/page?get=someparam is requested,
 * using &#064;RequestParam("get") will give you 
 * "someparam". To use this annotation in code, 
 * consider...
 * 
 * <pre>
 * &#064;HttpGet
 * public void getSomeParamFromUrl(&#064;RequestParam("get") String get){
 *     assertEquals("someparam", get);
 * }
 * </pre>
 * 
 * For &#064;RequestParam(value = "get", payloadType = RequestParam.JSON),
 * it will give you an object with the type same as
 * the argument that this annotation is attached to.
 * In the following example, http://somedomain.com/page?get={"name": "vincent"}
 * is requested.
 * <pre>
 * &#064;HttpGet
 * public void getSomeParamFromUrl(&#064;RequestParam(value = "get") Person person){
 *     assertEquals("vincent", person.name);
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    public enum Type{
        RAW,
        JSON
    }

    String value();
    
    RequestParam.Type payloadType() default RequestParam.Type.RAW;

    /**
     * whether this param is nullable or not.
     * If it is not nullable, 404 is returned.
     */
    boolean nullable() default false;
}