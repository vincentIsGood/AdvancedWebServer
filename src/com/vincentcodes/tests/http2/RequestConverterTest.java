package com.vincentcodes.tests.http2;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.http2.Http2Configuration;
import com.vincentcodes.webserver.http2.Http2FrameGenerator;
import com.vincentcodes.webserver.http2.Http2RequestConverter;
import com.vincentcodes.webserver.http2.errors.StreamError;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing class Http2RequestConverter")
public class RequestConverterTest {
    private Http2RequestConverter converter;
    private Http2FrameGenerator generator;

    @BeforeAll
    public void setup(){
        generator = new Http2FrameGenerator(new HpackEncoder(), new Http2Configuration());
        converter = new Http2RequestConverter(generator);
    }

    // Positives
    @Test
    public void headers_frame_only(){
        converter.addFrame(generator.requestHeadersFrame("GET", 1, true, true));
        Optional<HttpRequest> request = converter.toRequest();
        assertEquals("GET / HTTP/1.1\r\nhost: 127.0.0.1:5050\r\nuser-agent: curl/7.58.0\r\naccept: */*\r\n\r\n", request.get().toHttpString());
    }

    @Test
    public void headers_continuation_frames_one_each(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Length", "0");
        headers.add("Cache-Control", "no-cache");
        headers.add("Accept-Encoding", "en-Us,en");
        headers.add("Accept-Language", "gzip, deflate, br");
        
        converter.addFrame(generator.requestHeadersFrame("GET", 1, false, true));
        converter.addFrame(generator.continuationFrame(headers, 1, true));
        Optional<HttpRequest> request = converter.toRequest();
        assertTrue(request.get().toHttpString().contains("accept-encoding: en-Us,en"));
    }

    @Test
    public void headers_continuation_data_frames_one_each(){
        String payload = "asdasd";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Length", Integer.toString(payload.length()));
        headers.add("Content-Type", "text/plain");
        
        converter.addFrame(generator.requestHeadersFrame("POST", 1, false, true));
        converter.addFrame(generator.continuationFrame(headers, 1, true));
        converter.addFrame(generator.dataFrame(payload.getBytes(), 1, true));
        Optional<HttpRequest> request = converter.toRequest();
        assertTrue(request.get().toHttpString().contains("content-type: text/plain"));
        assertTrue(request.get().getBody().string().equals("asdasd"));
    }

    // Negatives
    @Test
    public void empty_buffer(){
        assertFalse(converter.toRequest().isPresent());
        converter.reset();
    }

    @Test
    public void unclosed_stream(){
        converter.addFrame(generator.requestHeadersFrame("GET", 1, true, false));
        Optional<HttpRequest> request = converter.toRequest();
        assertFalse(request.isPresent());
        converter.reset();
    }

    @Test
    public void unended_headers(){
        converter.addFrame(generator.requestHeadersFrame("GET", 1, false, true));
        Optional<HttpRequest> request = converter.toRequest();
        assertFalse(request.isPresent());
        converter.reset();
    }

    @Test
    public void unclosed_stream_after_2_frames(){
        converter.addFrame(generator.requestHeadersFrame("GET", 1, false, false));
        converter.addFrame(generator.continuationFrame(new HttpHeaders(), 1, true));
        Optional<HttpRequest> request = converter.toRequest();
        assertFalse(request.isPresent());
        converter.reset();
    }

    @Test
    public void unended_headers_on_continuation_frame(){
        converter.addFrame(generator.requestHeadersFrame("GET", 1, false, true));
        converter.addFrame(generator.continuationFrame(new HttpHeaders(), 1, false));
        Optional<HttpRequest> request = converter.toRequest();
        assertFalse(request.isPresent());
        converter.reset();
    }

    @Test
    public void absence_of_headers_throws_error(){
        converter.addFrame(generator.continuationFrame(new HttpHeaders(), 1, true));
        assertThrows(StreamError.class, () -> {
            converter.toRequest();
        });
        converter.reset();
    }
}
