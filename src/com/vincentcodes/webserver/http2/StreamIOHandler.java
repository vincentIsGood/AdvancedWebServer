package com.vincentcodes.webserver.http2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @see Http2Connection
 */
public interface StreamIOHandler {
    void accept(Http2Stream stream, Http2Frame frame) throws IOException, InvocationTargetException;
}
