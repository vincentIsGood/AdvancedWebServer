package com.vincentcodes.tests.http2;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.http2.Http2Configuration;
import com.vincentcodes.webserver.http2.Http2FrameGenerator;
import com.vincentcodes.webserver.http2.Http2Stream;
import com.vincentcodes.webserver.http2.constants.StreamState;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Testing class Http2Stream")
public class Http2StreamTest {
    private Http2FrameGenerator generator;
    private Http2Stream stream;

    @BeforeAll
    public void setup(){
        generator = new Http2FrameGenerator(new HpackEncoder(), new Http2Configuration());
        stream = newStream();
    }
    private Http2Stream newStream(){
        Http2Stream stream = new Http2Stream(1, (s, frame)->{}, (s, frame)->{}, generator);
        return stream;
    }

    @Test
    @Order(1)
    public void idle(){
        assertTrue(StreamState.IDLE == stream.getState());
    }

    @Test
    @Order(2)
    public void from_idle_to_open_state() throws Exception{
        stream.send(generator.responseHeadersFrame(200, 1, true, false));
        assertTrue(StreamState.OPEN == stream.getState());
    }

    @Test
    @Order(3)
    public void from_open_to_half_closed_local_state() throws Exception{
        stream.send(generator.dataFrame(new String("asd").getBytes(), 1, true));
        assertTrue(StreamState.HALF_CLOSED_LOCAL == stream.getState());
    }

    @Test
    @Order(4)
    public void from_half_closed_local_to_closed_state() throws Exception{
        stream.send(generator.rstStreamFrame(1));
        assertTrue(StreamState.CLOSED == stream.getState());
    }
    
    // Route 2
    @Test
    @Order(5)
    public void from_open_to_half_closed_remote_state() throws Exception{
        stream = newStream();
        stream.process(generator.requestHeadersFrame("get", 1, true, false));
        stream.process(generator.dataFrame(new String("asd").getBytes(), 1, true));
        assertTrue(StreamState.HALF_CLOSED_REMOTE == stream.getState());
    }

    @Test
    @Order(6)
    public void from_half_closed_remote_to_closed_state() throws Exception{
        stream.send(generator.rstStreamFrame(1));
        assertTrue(StreamState.CLOSED == stream.getState());
    }

    @Test
    @Order(7)
    public void one_header_with_continuation_frame_overall() throws Exception{
        stream = newStream();
        stream.process(generator.requestHeadersFrame("get", 1, false, true));
        stream.process(generator.continuationFrame(new HttpHeaders(), 1, true));
        assertTrue(StreamState.HALF_CLOSED_REMOTE == stream.getState());
    }
}
