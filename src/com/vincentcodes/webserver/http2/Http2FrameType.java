package com.vincentcodes.webserver.http2;

import java.io.IOException;
import java.io.OutputStream;

import com.vincentcodes.webserver.http2.hpack.HpackEncoder;

public interface Http2FrameType {
    /**
     * <p>
     * It's <b>optional</b> to implement it
     * <p>
     * Flags coming from parent may come in handy for 
     * {@link Http2FrameType#toBytes()}. You need to
     * create a variable to store that of course.
     * This method can be empty when the info from 
     * parent is not needed.
     */
    public void attach(Http2Frame parent);

    /**
     * <p>
     * It's <b>optional</b> to implement it
     * <p>
     * May be used in some specific frames
     */
    public void attach(HpackEncoder encoder);

    /**
     * @return the length of this frame type definition
     */
    public int length();
    
    public byte[] toBytes();

    public void streamBytesTo(OutputStream stream) throws IOException;
}
