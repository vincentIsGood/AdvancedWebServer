package com.vincentcodes.tests.http2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vincentcodes.webserver.exception.CannotParseRequestException;
import com.vincentcodes.webserver.http2.Http2Configuration;
import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameGenerator;
import com.vincentcodes.webserver.http2.Http2RequestParser;
import com.vincentcodes.webserver.http2.Http2TableEntry;
import com.vincentcodes.webserver.http2.SettingParameter;
import com.vincentcodes.webserver.http2.constants.CanonicalHuffmanCode;
import com.vincentcodes.webserver.http2.constants.FrameTypes;
import com.vincentcodes.webserver.http2.hpack.HpackCodecUtils;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;
import com.vincentcodes.webserver.http2.types.SettingsFrame;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing class HpackEncoderTest")
public class HpackEncoderTest {
    private HpackDecoder decoder;
    private HpackEncoder encoder;

    @BeforeAll
    public void setup(){
        encoder = new HpackEncoder();
        decoder = new HpackDecoder();
    }

    @Test
    public void encode_header_without_indexing() throws IOException{
        byte[] buf = new byte[]{
            (byte)0x04, (byte)0x0c, (byte)0x2f, (byte)0x73, (byte)0x61, (byte)0x6d, (byte)0x70, (byte)0x6c, 
            (byte)0x65, (byte)0x2f, (byte)0x70, (byte)0x61, (byte)0x74, (byte)0x68
        };
        ByteArrayInputStream is = new ByteArrayInputStream(buf);
        List<Http2TableEntry> result = decoder.decode(is);
        assertEquals(Arrays.toString(buf), Arrays.toString(encoder.encode(result, false)));
    }

    @Test
    public void encode_headers_incremental() throws IOException{
        System.out.println(Arrays.toString(HpackCodecUtils.encodeString("www.example.com")));
        byte[] buf = new byte[]{  
            (byte)0x82, (byte)0x86, (byte)0x84, (byte)0x41, (byte)0x0f, (byte)0x77, (byte)0x77, 
            (byte)0x77, (byte)0x2e, (byte)0x65, (byte)0x78, (byte)0x61, (byte)0x6d, (byte)0x70, 
            (byte)0x6c, (byte)0x65, (byte)0x2e, (byte)0x63, (byte)0x6f, (byte)0x6d
        };
        ByteArrayInputStream is = new ByteArrayInputStream(buf);
        List<Http2TableEntry> result = decoder.decode(is);
        assertEquals(Arrays.toString(buf), Arrays.toString(encoder.encodeWithIncremental(result, false)));
    }

    @Test
    public void encode_headers_response() throws IOException{
        HpackEncoder encoder = new HpackEncoder();
        HpackDecoder decoder = new HpackDecoder();
        System.out.println(Arrays.toString(HpackCodecUtils.encodeString("www.example.com")));
        byte[] buf = new byte[]{
            (byte)0x48, (byte)0x82, (byte)0x64, (byte)0x02, (byte)0x58, (byte)0x85, (byte)0xae, (byte)0xc3, (byte)0x77, (byte)0x1a, (byte)0x4b, (byte)0x61, (byte)0x96, (byte)0xd0, (byte)0x7a, (byte)0xbe,
            (byte)0x94, (byte)0x10, (byte)0x54, (byte)0xd4, (byte)0x44, (byte)0xa8, (byte)0x20, (byte)0x05, (byte)0x95, (byte)0x04, (byte)0x0b, (byte)0x81, (byte)0x66, (byte)0xe0, (byte)0x82, (byte)0xa6,
            (byte)0x2d, (byte)0x1b, (byte)0xff, (byte)0x6e, (byte)0x91, (byte)0x9d, (byte)0x29, (byte)0xad, (byte)0x17, (byte)0x18, (byte)0x63, (byte)0xc7, (byte)0x8f, (byte)0x0b, (byte)0x97, (byte)0xc8,
            (byte)0xe9, (byte)0xae, (byte)0x82, (byte)0xae, (byte)0x43, (byte)0xd3
        };
        ByteArrayInputStream is = new ByteArrayInputStream(buf);
        List<Http2TableEntry> result = decoder.decode(is);
        System.out.println(result);
        assertEquals(Arrays.toString(buf), Arrays.toString(encoder.encodeWithIncremental(result, true)));
    }

    @Test
    public void encode_huffmancode_test(){
        // 0xff, some padding "1" is added to LSB
        byte[] buf = new byte[]{
            (byte)0xf1, (byte)0xe3, (byte)0xc2, (byte)0xe5, (byte)0xf2, (byte)0x3a, 
            (byte)0x6b, (byte)0xa0, (byte)0xab, (byte)0x90, (byte)0xf4, (byte)0xff
        };
        assertEquals(Arrays.toString(buf), Arrays.toString(CanonicalHuffmanCode.encode("www.example.com")));
    }

    // This proves the base frame (Http2Frame) is not flawed (bytes taken from http2 client "curl")
    @Test
    public void encode_http2_frame_with_nullpayload() throws IOException{
        byte[] buf = new byte[]{
            0, 0, 0, 4, 1, 0, 0, 0, 0
        };
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.SETTINGS.value;
        frame.flags = SettingsFrame.ACK;
        frame.streamIdentifier = 0;
        frame.payloadLength = 0;
        assertEquals(Arrays.toString(buf), Arrays.toString(Http2Frame.toBytes(frame)));
    }

    @Test
    public void encode_http2_frame_with_settingsframe() throws IOException{
        byte[] buf = new byte[]{
            0, 0, 18, 4, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 100, 0, 4, 64, 0, 0, 0, 0, 2, 0, 0, 0, 0
        };
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.SETTINGS.value;
        frame.flags = 0;
        frame.streamIdentifier = 0;

        SettingsFrame settings = new SettingsFrame();
        List<SettingParameter> list = new ArrayList<>();
        list.add(new SettingParameter(3, 100));
        list.add(new SettingParameter(4, 1073741824));
        list.add(new SettingParameter(2, 0));
        settings.params = list;
        
        frame.payloadLength = settings.toBytes().length;
        frame.payload = settings;

        assertEquals(Arrays.toString(buf), Arrays.toString(Http2Frame.toBytes(frame)));
    }

    @Test
    public void encode_server_response_404() throws IOException, CannotParseRequestException{
        // 0xff, some padding "1" is added to LSB
        Http2Configuration conf = new Http2Configuration();
        Http2FrameGenerator frameGenerator = new Http2FrameGenerator(new HpackEncoder(), conf);
        Http2RequestParser parser = new Http2RequestParser(new HpackDecoder(), conf);
        byte[] buf = Http2Frame.toBytes(frameGenerator.responseHeadersFrame(404, 2, true, true));
        System.out.println(parser.parse(new ByteArrayInputStream(buf)));
    }
}
