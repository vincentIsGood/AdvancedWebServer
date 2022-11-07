package com.vincentcodes.webserver.annotaion.request;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Applies to custom made handlers to deal with specific paths.
 * RegEx is used to match the pattern of RequestMapping to the value of 
 * a real request path. 
 * <p>
 * You may use a SINGLE asterisk to match files from the same directory.
 * <pre>
 * // "/*" indicates all files in this directory
 * &#064;RequestMapping("/*")
 * public void handle(HttpRequest req, ResponseBuilder res){
 *  ...
 * }
 * </pre>
 * <p>
 * Use DOUBLE asterisk to match everything, even sub-directories.
 * <pre>
 * // "/**" indicates all files inside this directory recursively
 * &#064;RequestMapping("/**")
 * public File handle(HttpRequest req){
 *  ...
 * }
 * </pre>
 * <p>
 * Besides, you can add file extensions at the end of the super wildcard.
 * I call it "super wildcard", feel free to change the name.
 * <pre>
 * // "/**.mp4" indicates all mp4 files inside this directory recursively
 * &#064;RequestMapping("/**.mp4")
 * public File handle(HttpRequest req){
 *  ...
 * }
 * </pre>
 * <p>
 * Last but not least, you can add this annotation to classes.
 * <pre>
 * // It is not recommended to add a trailing forward slash "/" for class
 * // RequestMapping
 * &#064;RequestMapping("/samplewebsites")
 * class CustomHandler{
 *      &#064;RequestMapping("/readme.txt")
 *      public File handle(HttpRequest req){
 *       ...
 *      }
 * }
 * // "/samplewebpages/readme.txt" will NOT match the following handle method.
 * &#064;RequestMapping("/samplewebpages/**")
 * class CustomHandler2{
 *      &#064;RequestMapping("/readme.txt")
 *      public File handle(HttpRequest req){
 *       ...
 *      }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    public static RequestMapping ROOT_PATH = new RequestMapping(){
        public String value(){
            return "/";
        }
        public boolean exact(){
            return false;
        }
        public boolean raw(){
            return false;
        }

        @Override
        public Class<? extends Annotation> annotationType(){
            return RequestMapping.class;
        }
    };

    String value();
    boolean exact() default false; // exact match, no regex processing is conducted.
    boolean raw() default false;   // false => replaces * and **
}
