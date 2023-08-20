package com.vincentcodes.webserver.http2.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameType;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;

/**
 * @see https://tools.ietf.org/html/rfc7540#section-6.7
 */
public class PingFrame implements Http2FrameType {
    // flags
    public static byte ACK = 0x1; // must set this flag for responses

    public String opaqueData;

    @Override
    public void attach(Http2Frame parent){}

    @Override
    public void attach(HpackEncoder encoder){}

    @Override
    public int length(){
        return 8;
    }

    @Override
    public byte[] toBytes(){
        return new byte[8];
    }

    @Override
    public void streamBytesTo(OutputStream stream) throws IOException {
        stream.write(new byte[8]);
    }

    public static Http2FrameType parse(Http2Frame frame, InputStream is, HpackDecoder hpackDecoder) throws UncheckedIOException{
        if(frame.payloadLength != 8){
            frame.invalid();
            return null;
        }
        try{
            PingFrame pingFrame = new PingFrame();
            byte[] eightBytes = new byte[8];
            is.read(eightBytes);
            pingFrame.opaqueData = new String(eightBytes);
            return pingFrame;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public String toString(){
        return "{PingFrame}";
    }
}
