package com.vincentcodes.webserver.util;

/**
 * @param <A1> arg1
 * @param <A2> arg2
 * @param <A3> arg3
 * @param <R> the return value
 */
@FunctionalInterface
public interface TriFunction<A1, A2, A3, R> {
    R apply(A1 arg1, A2 arg2, A3 arg3);
}
