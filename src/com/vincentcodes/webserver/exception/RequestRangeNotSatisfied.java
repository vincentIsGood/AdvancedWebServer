package com.vincentcodes.webserver.exception;

public class RequestRangeNotSatisfied extends RuntimeException{
    
    private static final long serialVersionUID = 462105809895811383L;

    public RequestRangeNotSatisfied() {
        super();
    }

    public RequestRangeNotSatisfied(String message) {
        super(message);
    }

    public RequestRangeNotSatisfied(Throwable cause) {
        super(cause);
    }

    public RequestRangeNotSatisfied(String message, Throwable cause) {
        super(message, cause);
    }
}
