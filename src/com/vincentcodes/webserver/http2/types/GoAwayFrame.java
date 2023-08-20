package com.vincentcodes.webserver.http2.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import com.vincentcodes.webserver.helper.InputStreamHelper;
import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameType;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;
import com.vincentcodes.webserver.util.ByteUtils;

public class GoAwayFrame implements Http2FrameType {
    // GoAwayFrame has no flags
    
    // leftmost bit is a reserved bit
    public int lastStreamId;
    public long errorCode;
    public String debugData = "";

    @Override
    public void attach(Http2Frame parent){}
    
    @Override
    public void attach(HpackEncoder encoder){}

    @Override
    public int length(){
        return toBytes().length;
    }

    @Override
    public byte[] toBytes(){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            os.write(ByteUtils.intToByteArray(lastStreamId));
            os.write(ByteUtils.intToByteArray((int)errorCode));
            // ignores Additional Debug Data
        }catch(IOException e){}
        return os.toByteArray();
    }

    @Override
    public void streamBytesTo(OutputStream stream) throws IOException{
        stream.write(ByteUtils.intToByteArray(lastStreamId));
        stream.write(ByteUtils.intToByteArray((int)errorCode));
        // ignores Additional Debug Data
    }

    /**
     * @return the modified frame. (ie. Http2Frame from arg is the same as returned Http2Frame)
     */
    public static Http2FrameType parse(Http2Frame frame, InputStream is, HpackDecoder hpackDecoder) throws UncheckedIOException{
        if(frame.payloadLength < 8){
            frame.invalid();
            return null;
        }
        try{
            InputStreamHelper isUtil = new InputStreamHelper(is);
            GoAwayFrame goAwayFrame = new GoAwayFrame();
            // ignoring the leftmost reserved bit
            goAwayFrame.lastStreamId = ByteUtils.getIntFrom4Bytes(isUtil.getNextFourBytes(), 0);
            goAwayFrame.errorCode = ByteUtils.getLongFrom4Bytes(isUtil.getNextFourBytes(), 0);

            byte[] all = is.readAllBytes();
            goAwayFrame.debugData = new String(all).intern();
            // ignores Additional Debug Data
            return goAwayFrame;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public String toString(){
        return String.format("{GoAwayFrame lastStreamId: %d, errorCode: %d, debugData: %s}", lastStreamId, errorCode, debugData);
    }
}
