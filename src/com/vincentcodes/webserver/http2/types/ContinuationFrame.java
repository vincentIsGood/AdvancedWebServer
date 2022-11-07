package com.vincentcodes.webserver.http2.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameType;
import com.vincentcodes.webserver.http2.Http2TableEntry;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;

/**
 * @see https://tools.ietf.org/html/rfc7540#section-6.10
 */
public class ContinuationFrame implements Http2FrameType {
    // flags
    public static byte END_HEADERS = 0b00000100;

    public List<Http2TableEntry> headers;

    private HpackEncoder encoder;

    private ContinuationFrame(){}

    public ContinuationFrame(HpackEncoder encoder){
        this.encoder = encoder;
    }

    @Override
    public void attach(Http2Frame parent){}

    @Override
    public void attach(HpackEncoder encoder){
        this.encoder = encoder;
    }
    
    @Override
    public int length(){
        return toBytes().length;
    }

    @Override
    public byte[] toBytes(){
        return encoder.encode(headers, false);
    }
    
    public static Http2FrameType parse(Http2Frame frame, InputStream is, HpackDecoder hpackDecoder) throws UncheckedIOException{
        try{
            ContinuationFrame continuationFrame = new ContinuationFrame();
            byte[] headers = new byte[frame.payloadLength];
            is.read(headers);
            
            continuationFrame.headers = hpackDecoder.decode(headers);
            
            return continuationFrame;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public String toString(){
        return String.format("{ContinuationFrame headers: %s}", headers);
    }
}
