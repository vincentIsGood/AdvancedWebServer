package com.vincentcodes.tests.http2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import com.vincentcodes.webserver.http2.hpack.HpackCodecUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * @see https://tools.ietf.org/html/rfc7541#appendix-C.1
 */
@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing class HpackCodecUtils")
public class HpackCodecUtilsTest {
    @Test
    public void test_static_encodeInteger_5_bit_prefix_decoding_10() throws IOException{
        assertEquals(Arrays.toString(new byte[]{(byte)10}), Arrays.toString(HpackCodecUtils.encodeInteger(10, 5)));
    }

    @Test
    public void test_static_encodeInteger_5_bit_prefix_decoding_1337() throws IOException{
        assertEquals(Arrays.toString(new byte[]{(byte)31, (byte)154, (byte)10}), Arrays.toString(HpackCodecUtils.encodeInteger(1337, 5)));
    }

    @Test
    public void test_static_encodeInteger_8_bit_prefix_decoding_42() throws IOException{
        assertEquals(Arrays.toString(new byte[]{(byte)42}), Arrays.toString(HpackCodecUtils.encodeInteger(42, 8)));
    }

    @Test
    public void test_static_encodeString_short_string() throws IOException{
        byte[] encoded = HpackCodecUtils.encodeString("Hello World");
        String decoded = HpackCodecUtils.decodeString(new ByteArrayInputStream(encoded));
        assertEquals("Hello World", decoded);
    }

    @Test
    public void test_static_encodeString_long_string() throws IOException{
        String testStr = "Header field names and header field values can be represented as" + 
        "string literals.  A string literal is encoded as a sequence of" +
        "octets, either by directly encoding the string literal's octets";
        byte[] encoded = HpackCodecUtils.encodeString(testStr);
        String decoded = HpackCodecUtils.decodeString(new ByteArrayInputStream(encoded));
        assertEquals(testStr, decoded);
    }
    
    @Test
    public void test_static_decodeInteger_5_bit_prefix_decoding_10() throws IOException{
        assertEquals(10, HpackCodecUtils.decodeInteger(new byte[]{(byte)10}, 5)[1]);
    }

    @Test
    public void test_static_decodeInteger_5_bit_prefix_decoding_1337() throws IOException{
        assertEquals(1337, HpackCodecUtils.decodeInteger(new byte[]{(byte)31, (byte)154, (byte)10}, 5)[1]);
    }

    @Test
    public void test_static_decodeInteger_8_bit_prefix_decoding_42() throws IOException{
        assertEquals(42, HpackCodecUtils.decodeInteger(new byte[]{(byte)42}, 8)[1]);
    }
}
