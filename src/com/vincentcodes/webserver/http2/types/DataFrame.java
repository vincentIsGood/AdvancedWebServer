package com.vincentcodes.webserver.http2.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameType;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;

/**
 * @see https://tools.ietf.org/html/rfc7540#section-6.1
 */
public class DataFrame implements Http2FrameType{
    // flags specific to DATA frame
    public static byte END_STREAM = 0b00000001;
    public static byte PADDED     = 0b00001000;
    
    public byte[] data = null;

    public Http2Frame parent;

    public DataFrame(){}

    @Override
    public void attach(Http2Frame parent){
        this.parent = parent;
    }

    @Override
    public void attach(HpackEncoder encoder){}

    @Override
    public int length(){
        return toBytes().length;
    }
    
    /**
     * Do not support padding yet for the server yet.
     */
    @Override
    public byte[] toBytes(){
        return toBytes(this);
    }
    public static byte[] toBytes(DataFrame frame){
        ByteArrayOutputStream os = new ByteArrayOutputStream(); 
        byte paddingLength = (byte)(Math.random()*15);
        boolean padded = false;
        try{
            if(frame.parent != null){
                if((frame.parent.flags & PADDED) != 0){
                    os.write(paddingLength);
                    padded = true;
                }
            }
            os.write(frame.data);
            if(padded){
                os.write(new byte[paddingLength]);
            }
        }catch(IOException e){}
        return os.toByteArray();
    }

    @Override
    public void streamBytesTo(OutputStream stream) throws IOException{
        streamBytesTo(this, stream);
    }
    public static void streamBytesTo(DataFrame frame, OutputStream stream) throws IOException{
        byte paddingLength = (byte)(Math.random()*15);
        boolean padded = false;
        if(frame.parent != null){
            if((frame.parent.flags & PADDED) != 0){
                stream.write(paddingLength);
                padded = true;
            }
        }
        stream.write(frame.data);
        if(padded){
            stream.write(new byte[paddingLength]);
        }
    }

    /**
     * @return the modified frame. (ie. Http2Frame from arg is the same as returned Http2Frame)
     */
    public static Http2FrameType parse(Http2Frame frame, InputStream is, HpackDecoder hpackDecoder) throws UncheckedIOException{
        try{
            DataFrame dataFrame = new DataFrame();
            int paddingLength = 0;
            if((frame.flags & PADDED) > 0){
                paddingLength = is.read();
            }
            byte[] data = new byte[frame.payloadLength - paddingLength - (paddingLength > 0? 1 : 0)];
            is.read(data);
            dataFrame.data = data;
            is.skip(paddingLength);

            return dataFrame;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public String toString(){
        return String.format("{DataFrame data: %s}", data);
    }
}
