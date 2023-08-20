package com.vincentcodes.webserver.http2.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameType;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;
import com.vincentcodes.webserver.util.ByteUtils;

/**
 * @see https://tools.ietf.org/html/rfc7540#section-6.4
 */
public class RstStreamFrame implements Http2FrameType {
    // RstStreamFrame has no flags

    public long errorCode;

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
        return ByteUtils.intToByteArray((int)(errorCode & 0xffffffff));
    }

    @Override
    public void streamBytesTo(OutputStream stream) throws IOException {
        stream.write(ByteUtils.intToByteArray((int)(errorCode & 0xffffffff)));
    }

    public static Http2FrameType parse(Http2Frame frame, InputStream is, HpackDecoder hpackDecoder) throws UncheckedIOException{
        if(frame.payloadLength != 4){
            frame.invalid();
            return null;
        }
        try{
            RstStreamFrame rstStreamFrame = new RstStreamFrame();
            byte[] fourBytes = new byte[4];
            is.read(fourBytes);
            rstStreamFrame.errorCode = ByteUtils.getIntFrom4Bytes(fourBytes, 0);
            return rstStreamFrame;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public String toString(){
        return String.format("{RstStreamFrame errorCode: %d}", errorCode);
    }
}
