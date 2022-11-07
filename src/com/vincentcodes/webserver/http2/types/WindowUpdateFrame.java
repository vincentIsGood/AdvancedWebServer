package com.vincentcodes.webserver.http2.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameType;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;
import com.vincentcodes.webserver.util.ByteUtils;

/**
 * This frame is used for flow control. Whenever you want to send
 * DATA frames, you need permission / quota which approved to you 
 * by the endpoint (client). So, the endpoint will send you multiple
 * WINDOW_UPDATE frames to make sure you can keep sending things.
 * <p>
 * Each time DATA frame is sent, the window size decreases
 * <p>
 * The flow-control window is a simple integer value that indicates 
 * how many octets of data the <b>sender</b> is permitted
 * @see https://tools.ietf.org/html/rfc7540#section-6.9
 */
public class WindowUpdateFrame implements Http2FrameType {
    // WindowUpdateFrame has no flags

    // the leftmost bit is a reserved bit (ignored)
    public int windowSizeIncrement;

    @Override
    public void attach(Http2Frame parent){}

    @Override
    public void attach(HpackEncoder encoder){}

    @Override
    public int length(){
        return 4;
    }

    @Override
    public byte[] toBytes(){
        return ByteUtils.intToByteArray(windowSizeIncrement);
    }

    /**
     * @return the modified frame. (ie. Http2Frame from arg is the same as returned Http2Frame)
     */
    public static Http2FrameType parse(Http2Frame frame, InputStream is, HpackDecoder hpackDecoder) throws UncheckedIOException{
        if(frame.payloadLength != 4){
            frame.invalid();
            return null;
        }
        try{
            WindowUpdateFrame windowUpdateFrame = new WindowUpdateFrame();
            byte[] fourBytes = new byte[4];
            is.read(fourBytes);
            // ignoring the leftmost reserved bit
            windowUpdateFrame.windowSizeIncrement = ByteUtils.getIntFrom4Bytes(fourBytes, 0);
            return windowUpdateFrame;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public String toString(){
        return String.format("{WindowUpdateFrame windowSizeIncrement: %d}", windowSizeIncrement);
    }
}
