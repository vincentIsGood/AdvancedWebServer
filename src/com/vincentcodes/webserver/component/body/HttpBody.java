package com.vincentcodes.webserver.component.body;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import com.vincentcodes.webserver.component.header.EntityEncodings;

public interface HttpBody extends Closeable{
    /**
     * @return null if the provided accepted encoding is not supported
     */
    public EntityEncodings getAcceptedEncoding();

    public void writeToBody(int b) throws IOException;

    public void writeToBody(byte[] b) throws IOException;

    public void writeToBody(byte[] b, int length) throws IOException;

    /**
     * @param maxCap Max bytes write count; -1 is considered unlimited
     */
    public void maxCapacity(int maxCap);

    public byte[] getBytes();

    public byte[] getBytes(int length);

    public int getBytes(byte[] buffer);

    /**
     * Available bytes to be read from the stream
     */
    public int available() throws IOException;

    public String string();

    public void streamBytesTo(OutputStream os) throws IOException;

    public long length();
}
