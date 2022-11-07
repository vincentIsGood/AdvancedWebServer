package com.vincentcodes.webserver.http2.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameType;
import com.vincentcodes.webserver.http2.Http2TableEntry;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;
import com.vincentcodes.webserver.util.ByteUtils;

/**
 * @see https://tools.ietf.org/html/rfc7540#section-6.2
 */
public class HeadersFrame implements Http2FrameType {
    // flags
    public static byte END_STREAM  = 0b00000001; // last frame of the stream (excluding continuation frame)
    public static byte END_HEADERS = 0b00000100; // no continuation frame
    public static byte PADDED      = 0b00001000;
    public static byte PRIORITY    = 0b00100000;
    
    /**
     * Requires PRIORITY flag
     */
    public byte priorityBit = 0; // 0 or 1

    /**
     * Requires PRIORITY flag
     */
    public int streamDependency = -1; // 31 bits

    /**
     * Requires PRIORITY flag
     */
    public int priorityWeight = -1; // 1 byte

    /**
     * Currently, you need to create the list yourself
     */
    public List<Http2TableEntry> headers;

    private Http2Frame parent;
    private HpackEncoder encoder;

    public HeadersFrame(){}

    public HeadersFrame(Http2Frame parent, HpackEncoder encoder){
        this.parent = parent;
        this.encoder = encoder;
    }

    @Override
    public void attach(Http2Frame parent){
        this.parent = parent;
    }

    @Override
    public void attach(HpackEncoder encoder){
        this.encoder = encoder;
    }

    @Override
    public int length(){
        return toBytes().length;
    }

    /**
     * Not fully implemented (priority part)
     */
    @Override
    public byte[] toBytes(){
        ByteArrayOutputStream os = new ByteArrayOutputStream(); 
        byte paddingLength = (byte)(Math.random()*15);
        boolean padded = false;
        try{
            if(parent != null){
                if((parent.flags & PADDED) != 0){
                    os.write(paddingLength);
                    padded = true;
                }
                if((parent.flags & PRIORITY) != 0){
                    // error may occur if the value gets too large
                    os.write(ByteUtils.intToByteArray((priorityBit << 31) & streamDependency));
                    os.write((byte)priorityWeight);
                }
            }
            os.write(encoder.encode(headers, true));
            if(padded){
                os.write(new byte[paddingLength]);
            }
        }catch(IOException e){}
        return os.toByteArray();
    }
    
    public static Http2FrameType parse(Http2Frame frame, InputStream is, HpackDecoder hpackDecoder) throws UncheckedIOException{
        try{
            HeadersFrame headersFrame = new HeadersFrame();
            int bytesRead = 0;
            byte[] nextFourBytes = new byte[4];
            int paddingLength = 0;
            if((frame.flags & PADDED) != 0){
                paddingLength = is.read();
                bytesRead++;
            }

            if((frame.flags & PRIORITY) != 0){
                is.read(nextFourBytes);
                int nextFour = ByteUtils.getIntFrom4Bytes(nextFourBytes, 0);
                headersFrame.priorityBit = (byte)Math.abs(((nextFour & 0x80000000) >> 31));
                headersFrame.streamDependency = nextFour & 0x7fffffff; // exclude the left most bit

                headersFrame.priorityWeight = is.read();

                bytesRead+=5;
            }

            byte[] headers = new byte[frame.payloadLength-bytesRead-paddingLength];
            is.read(headers);
            
            headersFrame.headers = hpackDecoder.decode(headers);
            
            is.skip(paddingLength);
            return headersFrame;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public String toString(){
        return String.format("{HeadersFrame priorityBit: %d, streamDependency: %d, priorityWeight: %d, headers: %s}", priorityBit, streamDependency, priorityWeight, headers);
    }
}
