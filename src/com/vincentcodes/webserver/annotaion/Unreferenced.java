package com.vincentcodes.webserver.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It is used to ensure a value is properly used
 * and not reported as a warning by your editor.
 * I used it to make sure some packages are 
 * compiled since it is linked to the main code.
 * It's my problem to use command line to parse
 * code easily.
 * 
 * @author Vincent Ko
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
@Repeatable(MultipleUnreferenced.class)
public @interface Unreferenced {
    Class<?> value();
}