package com.vincentcodes.webserver.dispatcher.reflect;

/**
 * This is mainly used by annotations.
 * @see com.vincentcodes.webserver.annotaion.wrapper.InvocationCondition
 */
@FunctionalInterface
public interface ConditionalWrapper {
    public boolean evaluate(DispatcherEnvironment env);
}
