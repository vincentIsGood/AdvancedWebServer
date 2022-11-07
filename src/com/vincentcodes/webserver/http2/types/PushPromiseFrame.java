package com.vincentcodes.webserver.http2.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import com.vincentcodes.webserver.helper.InputStreamHelper;
import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameType;
import com.vincentcodes.webserver.http2.Http2TableEntry;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;
import com.vincentcodes.webserver.util.ByteUtils;

/**
 * A client cannot push. Thus, servers MUST treat the receipt of a 
 * PUSH_PROMISE frame as a connection error.
 * <p>
 * What is the purpose of Push Requests? See this article...
 * https://developers.google.com/web/fundamentals/performance/http2
 * 
 * @see https://tools.ietf.org/html/rfc7540#section-6.6
 * @see https://tools.ietf.org/html/rfc7540#section-8.2
 */
public class PushPromiseFrame implements Http2FrameType {
    // flags
    public static byte END_HEADERS = 0b00000100;
    public static byte PADDED      = 0b00001000;

    // ignoring the leftmost reserved bit
    public int promisedStreamId;
    public List<Http2TableEntry> headers;

    private Http2Frame parent;
    private HpackEncoder encoder;

    public PushPromiseFrame(){}

    public PushPromiseFrame(Http2Frame parent, HpackEncoder encoder){
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
                os.write(ByteUtils.intToByteArray(promisedStreamId));
            }
            os.write(encoder.encode(headers, false));
            if(padded){
                os.write(new byte[paddingLength]);
            }
        }catch(IOException e){}
        return os.toByteArray();
    }

    public static Http2FrameType parse(Http2Frame frame, InputStream is, HpackDecoder hpackDecoder) throws UncheckedIOException{
        try{
            InputStreamHelper isUtil = new InputStreamHelper(is);
            PushPromiseFrame pushPromiseFrame = new PushPromiseFrame();
            int paddingLength = 0;
            int bytesRead = 0;
            if((frame.flags & PADDED) != 0){
                paddingLength = is.read();
                bytesRead++;
            }
            pushPromiseFrame.promisedStreamId = ByteUtils.getIntFrom4Bytes(isUtil.getNextFourBytes(), 0);
            bytesRead+=4;

            byte[] headers = new byte[frame.payloadLength-bytesRead-paddingLength];
            is.read(headers);
            
            pushPromiseFrame.headers = hpackDecoder.decode(headers);

            is.skip(paddingLength);
            return pushPromiseFrame;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public String toString(){
        return String.format("{PushPromiseFrame promisedStreamId: %d, headers: %s}", promisedStreamId, headers);
    }
}
