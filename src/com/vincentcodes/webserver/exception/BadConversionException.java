package com.vincentcodes.webserver.exception;

/**
 * Used to wrap MalformedURLException into a RuntimeException.
 */
public class BadConversionException extends RuntimeException {
    private static final long serialVersionUID = 6734340907488456685L;

    public BadConversionException() {
    }

    public BadConversionException(String message) {
        super(message);
    }

    public BadConversionException(Throwable cause) {
        super(cause);
    }

    public BadConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
