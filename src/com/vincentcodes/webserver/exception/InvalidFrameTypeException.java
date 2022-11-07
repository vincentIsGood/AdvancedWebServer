package com.vincentcodes.webserver.exception;

public class InvalidFrameTypeException extends CannotParseRequestException {
    private static final long serialVersionUID = 2673223140932421899L;

    public InvalidFrameTypeException() {
    }

    public InvalidFrameTypeException(String message) {
        super(message);
    }

    public InvalidFrameTypeException(Throwable cause) {
        super(cause);
    }

    public InvalidFrameTypeException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
