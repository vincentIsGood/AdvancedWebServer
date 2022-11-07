package com.vincentcodes.webserver.http2.errors;

import java.io.IOException;

public class DecodeError extends IOException {
    public DecodeError() {
        super();
    }

    public DecodeError(String message) {
        super(message);
    }

    public DecodeError(Throwable cause) {
        super(cause);
    }

    public DecodeError(String message, Throwable cause) {
        super(message, cause);
    }
}
