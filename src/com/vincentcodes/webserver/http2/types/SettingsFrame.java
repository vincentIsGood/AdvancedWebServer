package com.vincentcodes.webserver.http2.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vincentcodes.webserver.helper.InputStreamHelper;
import com.vincentcodes.webserver.http2.Http2Configuration;
import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameType;
import com.vincentcodes.webserver.http2.SettingParameter;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;
import com.vincentcodes.webserver.util.ByteUtils;

/**
 * Used to tell the endpoints what they should do.
 * @see Http2Configuration
 * @see https://tools.ietf.org/html/rfc7540#section-6.5
 */
public class SettingsFrame implements Http2FrameType {
    // flags
    /**
     * <p>
     * ACK (0x1):  When set, bit 0 indicates that this frame acknowledges 
     * receipt and application of the peer's SETTINGS frame.  When this
     * bit is set, the payload of the SETTINGS frame MUST be empty. For 
     * more info, see https://tools.ietf.org/html/rfc7540#section-6.5.3
     * <p>
     * 6.5.3.  Settings Synchronization
     * <p>
     * Upon receiving a SETTINGS frame with the ACK flag set, the
     * sender of the altered parameters can rely on the setting having been
     * applied
     */
    public static byte ACK = 0x1;

    // parameters / identifiers
    public static int SETTINGS_HEADER_TABLE_SIZE = 0x1;      // value default: 4096
    public static int SETTINGS_ENABLE_PUSH = 0x2;            // value default: 1 (enable)
    public static int SETTINGS_MAX_CONCURRENT_STREAMS = 0x3; // value default: unlimited
    public static int SETTINGS_INITIAL_WINDOW_SIZE = 0x4;    // value default: 2^16-1 (65535 bytes)
    public static int SETTINGS_MAX_FRAME_SIZE = 0x5;         // value default: 2^14 (16384 bytes)
    public static int SETTINGS_MAX_HEADER_LIST_SIZE = 0x6;   // value default: unlimited

    public List<SettingParameter> params; // an UnmodifiableList

    public SettingsFrame(){
        this.params = new ArrayList<>();
    }

    @Override
    public void attach(Http2Frame parent){}

    @Override
    public void attach(HpackEncoder encoder){}

    @Override
    public int length(){
        return params.size()*6;
    }

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            for(SettingParameter param : params){
                os.write(ByteUtils.shortToByteArray((short)param.getIdentifier()));
                os.write(ByteUtils.intToByteArray((int)param.getValue()));
            }
        }catch(IOException e){}
        return os.toByteArray();
    }

    @Override
    public void streamBytesTo(OutputStream stream) throws IOException {
        for(SettingParameter param : params){
            stream.write(ByteUtils.shortToByteArray((short)param.getIdentifier()));
            stream.write(ByteUtils.intToByteArray((int)param.getValue()));
        }
    }
    
    public static Http2FrameType parse(Http2Frame frame, InputStream is, HpackDecoder hpackDecoder) throws UncheckedIOException{
        if(frame.payloadLength % 6 != 0){
            return null;
        }
        try{
            InputStreamHelper isUtil = new InputStreamHelper(is);
            SettingsFrame settingsFrame = new SettingsFrame();
            //since it's divisible by 6
            for(int i = 0; i < frame.payloadLength/6; i++){
                settingsFrame.params.add(new SettingParameter(
                    ByteUtils.getIntFrom2Bytes(isUtil.getNextTwoBytes(), 0), 
                    ByteUtils.getLongFrom4Bytes(isUtil.getNextFourBytes(), 0)));
            }
            settingsFrame.params = Collections.unmodifiableList(settingsFrame.params);
            return settingsFrame;
        }catch(IOException e){
            throw new UncheckedIOException(e);
        }
    }

    public String toString(){
        return String.format("{SettingsFrame params: %s}", params);
    }
}