package com.vincentcodes.webserver.http2.errors;

// TODO: this must be a checked exception
public class StreamError extends RuntimeException {
    public StreamError() {
        super();
    }

    public StreamError(String message) {
        super(message);
    }

    public StreamError(Throwable cause) {
        super(cause);
    }

    public StreamError(String message, Throwable cause) {
        super(message, cause);
    }
}
