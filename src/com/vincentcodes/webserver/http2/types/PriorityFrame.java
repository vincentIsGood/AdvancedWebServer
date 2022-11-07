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
 * @see https://tools.ietf.org/html/rfc7540#section-6.3
 */
public class PriorityFrame implements Http2FrameType {
    // Priority frame has no flags
    
    public byte exclusiveStreamBit = 0;
    public int streamDependency = -1; // 31 bits
    public int priorityWeight = -1;

    @Override
    public void attach(Http2Frame parent){}

    @Override
    public void attach(HpackEncoder encoder){}

    @Override
    public int length(){
        return 5;
    }

    @Override
    public byte[] toBytes(){
        byte[] intSized = ByteUtils.intToByteArray((exclusiveStreamBit << 31) | streamDependency);
        return new byte[]{intSized[0], intSized[1], intSized[2], intSized[3], (byte)priorityWeight};
    }

    public static Http2FrameType parse(Http2Frame frame, InputStream is, HpackDecoder hpackDecoder) throws UncheckedIOException{
        if(frame.payloadLength != 5){
            frame.invalid();
            return null;
        }
        try{
            PriorityFrame priorityFrame = new PriorityFrame();
            byte[] nextFourBytes = new byte[4];
            is.read(nextFourBytes);
            int nextFour = ByteUtils.getIntFrom4Bytes(nextFourBytes, 0);
            priorityFrame.exclusiveStreamBit = (byte)Math.abs(((nextFour & 0x80000000) >> 31));
            priorityFrame.streamDependency = nextFour & 0x7fffffff; // exclude the left most bit

            priorityFrame.priorityWeight = is.read();
            return priorityFrame;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public String toString(){
        return String.format("{PriorityFrame exclusiveStreamBit: %d, streamDependency: %d, priorityWeight: %d}", exclusiveStreamBit, streamDependency, priorityWeight);
    }
}
