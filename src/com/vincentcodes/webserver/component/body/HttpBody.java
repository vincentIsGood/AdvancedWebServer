package com.vincentcodes.webserver.component.body;

import java.io.IOException;

import com.vincentcodes.webserver.component.header.EntityEncodings;

public interface HttpBody {
    /**
     * @return null if the provided accepted encoding is not supported
     */
    public EntityEncodings getAcceptedEncoding();

    public void writeToBody(int b) throws IOException;

    public void writeToBody(byte[] b) throws IOException;

    /**
     * @param maxCap Max bytes write count; -1 is considered unlimited
     */
    public void maxCapacity(int maxCap);

    /**
     * This causes the DeflatedOutputStream to call finish.
     */
    public byte[] getBytes();

    /**
     * Returns null if DeflatedOutputStream is used.
     */
    public String string();

    public int length();
}
