package com.vincentcodes.webserver.http2.errors;

public class ConnectionError extends RuntimeException {
    public ConnectionError() {
        super();
    }

    public ConnectionError(String message) {
        super(message);
    }

    public ConnectionError(Throwable cause) {
        super(cause);
    }

    public ConnectionError(String message, Throwable cause) {
        super(message, cause);
    }
}
