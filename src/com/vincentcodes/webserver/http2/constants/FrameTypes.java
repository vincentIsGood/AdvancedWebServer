package com.vincentcodes.webserver.http2.constants;

import java.io.InputStream;

import com.vincentcodes.webserver.http2.Http2Frame;
import com.vincentcodes.webserver.http2.Http2FrameType;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.types.ContinuationFrame;
import com.vincentcodes.webserver.http2.types.DataFrame;
import com.vincentcodes.webserver.http2.types.GoAwayFrame;
import com.vincentcodes.webserver.http2.types.HeadersFrame;
import com.vincentcodes.webserver.http2.types.PingFrame;
import com.vincentcodes.webserver.http2.types.PriorityFrame;
import com.vincentcodes.webserver.http2.types.PushPromiseFrame;
import com.vincentcodes.webserver.http2.types.RstStreamFrame;
import com.vincentcodes.webserver.http2.types.SettingsFrame;
import com.vincentcodes.webserver.http2.types.WindowUpdateFrame;
import com.vincentcodes.webserver.util.TriFunction;

/**
 * @see https://tools.ietf.org/html/rfc7540#section-11.2
 */
public enum FrameTypes {
    DATA          ((byte) 0x0, DataFrame::parse), // convey arbitrary, variable-length sequences of octets
    HEADERS       ((byte) 0x1, HeadersFrame::parse), // opens a stream
    PRIORITY      ((byte) 0x2, PriorityFrame::parse), // specifies sender-advised priority of a stream
    RST_STREAM    ((byte) 0x3, RstStreamFrame::parse), // immediate terminate a stream
    SETTINGS      ((byte) 0x4, SettingsFrame::parse), // config
    PUSH_PROMISE  ((byte) 0x5, PushPromiseFrame::parse), // notify the intention to initiate streams
    PING          ((byte) 0x6, PingFrame::parse), // used to measure a minimal round-trip time from the sender
    GOAWAY        ((byte) 0x7, GoAwayFrame::parse), // initiate shutdown of a connection or to signal serious error conditions
    WINDOW_UPDATE ((byte) 0x8, WindowUpdateFrame::parse), // used to implement flow control
    CONTINUATION  ((byte) 0x9, ContinuationFrame::parse); // continue a sequence of header block fragments

    public final byte value;
    public final TriFunction<Http2Frame, InputStream, HpackDecoder, Http2FrameType> parseFunc; // may throw UncheckedIOException 

    FrameTypes(byte type, TriFunction<Http2Frame, InputStream, HpackDecoder, Http2FrameType> func){
        this.value = type;
        this.parseFunc = func;
    }

    /**
     * @param b
     * @return null if not found
     */
    public static FrameTypes fromByte(byte b){
        for(FrameTypes ele : values()) if(ele.value == b) return ele;
        return null;
    }
}
