package com.vincentcodes.webserver.dispatcher;

import com.vincentcodes.webserver.dispatcher.operation.impl.HttpInvocationStrategy;

/**
 * @see HttpInvocationStrategy#handleResponseBody
 */
public class HttpHandlerResult {
    // nullable
    private Object returnResult;

    private HttpHandlerOptions options;

    public HttpHandlerResult(Object returnResult){
        this(returnResult, HttpHandlerOptions.empty());
    }

    /**
     * @param options recommend the use of {@link HttpHandlerOptions.Builder} to build the options
     */
    public HttpHandlerResult(Object returnResult, HttpHandlerOptions options){
        this.returnResult = returnResult;
        this.options = options;
    }

    public Object getReturnResult() {
        return returnResult;
    }

    public HttpHandlerOptions getOptions() {
        return options;
    }

}
