package com.vincentcodes.webserver.helper;

/**
 * @param <V> the object to be inspected by the filter
 */
public interface Filterable<V> {
    /**
     * @return whether the object should be filtered out or not.
     */
    boolean willFilterOut(V arg);
}
