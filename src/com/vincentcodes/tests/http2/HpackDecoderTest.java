package com.vincentcodes.tests.http2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.vincentcodes.webserver.http2.DynamicTable;
import com.vincentcodes.webserver.http2.Http2Configuration;
import com.vincentcodes.webserver.http2.Http2TableEntry;
import com.vincentcodes.webserver.http2.StaticTable;
import com.vincentcodes.webserver.http2.constants.CanonicalHuffmanCode;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @see https://tools.ietf.org/html/rfc7541#appendix-C.3.1
 */
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Testing class HpackDecoderTest")
public class HpackDecoderTest {
    private HpackDecoder decoder;
    private DynamicTable dynamicTable;

    @BeforeAll
    public void setup(){
        // use default configuration for the test.
        dynamicTable = new DynamicTable(new Http2Configuration(), StaticTable.instance.size()+1);
        decoder = new HpackDecoder(dynamicTable);
    }

    @Test
    @Order(1)
    public void decode_headers_test() throws IOException{
        byte[] buf = new byte[]{
            (byte)0x82, (byte)0x86, (byte)0x84, (byte)0x41, (byte)0x0f, (byte)0x77, (byte)0x77, 
            (byte)0x77, (byte)0x2e, (byte)0x65, (byte)0x78, (byte)0x61, (byte)0x6d, (byte)0x70, 
            (byte)0x6c, (byte)0x65, (byte)0x2e, (byte)0x63, (byte)0x6f, (byte)0x6d
        };
        ByteArrayInputStream is = new ByteArrayInputStream(buf);
        List<Http2TableEntry> result = decoder.decode(is);
        assertEquals(1, dynamicTable.size());
        assertTrue(result.size() > 0);
    }

    @Test
    @Order(2)
    public void decode_headers_test2() throws IOException{
        byte[] buf = new byte[]{
            (byte)0x82, (byte)0x86, (byte)0x84, (byte)0xbe, (byte)0x58, (byte)0x08, (byte)0x6e, 
            (byte)0x6f, (byte)0x2d, (byte)0x63, (byte)0x61, (byte)0x63, (byte)0x68, (byte)0x65
        };
        ByteArrayInputStream is = new ByteArrayInputStream(buf);
        List<Http2TableEntry> result = decoder.decode(is);
        assertEquals(2, dynamicTable.size());
        assertTrue(result.size() > 0);
    }

    @Test
    @Order(3)
    public void decode_headers_test3() throws IOException{
        byte[] buf = new byte[]{
            (byte)0x82, (byte)0x87, (byte)0x85, (byte)0xbf, (byte)0x40, (byte)0x0a, (byte)0x63, (byte)0x75, 
            (byte)0x73, (byte)0x74, (byte)0x6f, (byte)0x6d, (byte)0x2d, (byte)0x6b, (byte)0x65, (byte)0x79,
            (byte)0x0c, (byte)0x63, (byte)0x75, (byte)0x73, (byte)0x74, (byte)0x6f, (byte)0x6d, (byte)0x2d, 
            (byte)0x76, (byte)0x61, (byte)0x6c, (byte)0x75, (byte)0x65
        };
        ByteArrayInputStream is = new ByteArrayInputStream(buf);
        List<Http2TableEntry> result = decoder.decode(is);
        assertEquals(3, dynamicTable.size());
        assertTrue(result.size() > 0);
    }

    @Test
    public void decode_huffmancode_test() {
        byte[] buf = new byte[]{
            (byte)0xf1, (byte)0xe3, (byte)0xc2, (byte)0xe5, (byte)0xf2, (byte)0x3a, 
            (byte)0x6b, (byte)0xa0, (byte)0xab, (byte)0x90, (byte)0xf4, (byte)0xff
        };
        assertEquals("www.example.com", CanonicalHuffmanCode.decode(buf));
    }
}
