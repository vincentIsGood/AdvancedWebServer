package com.vincentcodes.webserver.exception;

public class UnsupportedHttpMethodException extends CannotParseRequestException {

    private static final long serialVersionUID = -8104095833586038979L;

    public UnsupportedHttpMethodException() {
        super();
    }

    public UnsupportedHttpMethodException(String message) {
        super(message);
    }

    public UnsupportedHttpMethodException(Throwable cause) {
        super(cause);
    }

    public UnsupportedHttpMethodException(String message, Throwable cause) {
        super(message, cause);
    }
}
