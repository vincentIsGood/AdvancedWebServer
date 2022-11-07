package com.vincentcodes.webserver.exception;

public class CannotParseRequestException extends Exception {

    private static final long serialVersionUID = 8581596437816461534L;

    public CannotParseRequestException() {
        super();
    }

    public CannotParseRequestException(String message) {
        super(message);
    }

    public CannotParseRequestException(Throwable cause) {
        super(cause);
    }

    public CannotParseRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
