package com.vincentcodes.webserver.http2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.http2.constants.ErrorCodes;
import com.vincentcodes.webserver.http2.constants.FrameTypes;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;
import com.vincentcodes.webserver.http2.types.ContinuationFrame;
import com.vincentcodes.webserver.http2.types.DataFrame;
import com.vincentcodes.webserver.http2.types.GoAwayFrame;
import com.vincentcodes.webserver.http2.types.HeadersFrame;
import com.vincentcodes.webserver.http2.types.PingFrame;
import com.vincentcodes.webserver.http2.types.RstStreamFrame;
import com.vincentcodes.webserver.http2.types.SettingsFrame;
import com.vincentcodes.webserver.http2.types.WindowUpdateFrame;

/**
 * A lightweight frame generator used to generate 
 * common http2 frames. Feel free to create your own.
 */
public class Http2FrameGenerator {
    private HpackEncoder encoder;
    private Http2Configuration config;
    
    public Http2FrameGenerator(HpackEncoder encoder, Http2Configuration config){
        this.encoder = encoder;
        this.config = config;
    }

    public Http2Configuration getConfig(){
        return config;
    }

    public Http2Frame settingsFrameAck(int streamId){
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.SETTINGS.value;
        frame.flags = SettingsFrame.ACK;
        frame.streamIdentifier = streamId;

        SettingsFrame settings = new SettingsFrame();
        frame.payloadLength = settings.length();
        frame.payload = settings;

        return frame;
    }
    
    /**
     * Not fully implemented
     */
    public Http2Frame settingsFrame(int streamId){
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.SETTINGS.value;
        frame.flags = 0;
        frame.streamIdentifier = streamId;

        SettingsFrame settings = new SettingsFrame();
        settings.params = convertConfigToSettings(config);

        frame.payloadLength = settings.length();
        frame.payload = settings;
        
        return frame;
    }

    /**
     * Response.
     * @param headers [nullable]
     */
    public Http2Frame responseHeadersFrame(int status, HttpHeaders headers, int streamId, boolean endHeaders, boolean endStream){
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.HEADERS.value;
        frame.flags = (byte)((endHeaders? HeadersFrame.END_HEADERS : 0) | (endStream? HeadersFrame.END_STREAM : 0));
        frame.streamIdentifier = streamId;

        HeadersFrame headersFrame = new HeadersFrame(frame, encoder);

        List<Http2TableEntry> listHeaders = new ArrayList<>();
        listHeaders.add(new Http2TableEntry(":status", Integer.toString(status)));
        if(headers == null){
            listHeaders.add(new Http2TableEntry("server", "vws"));
            listHeaders.add(new Http2TableEntry("date", WebServer.DATE_FORMAT.format(new Date())));
            listHeaders.add(new Http2TableEntry("cache-control", "no-store"));
        }else{
            listHeaders.addAll(convertHeadersToEntries(headers));
        }

        headersFrame.headers = listHeaders;
        frame.payloadLength = headersFrame.length();
        frame.payload = headersFrame;

        return frame;
    }
    /**
     * @see #responseHeadersFrame(int, HttpHeaders, int, boolean, boolean)
     */
    public Http2Frame responseHeadersFrame(int status, int streamId, boolean endHeaders, boolean endStream){
        return responseHeadersFrame(status, null, streamId, endHeaders, endStream);
    }

    /**
     * Request. (Currently used for testing purposes)
     */
    public Http2Frame requestHeadersFrame(String method, int streamId, boolean endHeaders, boolean endStream){
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.HEADERS.value;
        frame.flags = (byte)((endHeaders? HeadersFrame.END_HEADERS : 0) | (endStream? HeadersFrame.END_STREAM : 0));
        frame.streamIdentifier = streamId;

        HeadersFrame headersFrame = new HeadersFrame(frame, encoder);

        List<Http2TableEntry> listHeaders = new ArrayList<>();
        listHeaders.add(new Http2TableEntry(":method", method.toUpperCase()));
        listHeaders.add(new Http2TableEntry(":path", "/"));
        listHeaders.add(new Http2TableEntry(":scheme", "https"));
        listHeaders.add(new Http2TableEntry(":authority", "127.0.0.1:5050"));
        listHeaders.add(new Http2TableEntry("user-agent", "curl/7.58.0"));
        listHeaders.add(new Http2TableEntry("accept", "*/*"));

        headersFrame.headers = listHeaders;
        frame.payloadLength = headersFrame.length();
        frame.payload = headersFrame;

        return frame;
    }

    public Http2Frame continuationFrame(HttpHeaders headers, int streamId, boolean endHeaders){
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.CONTINUATION.value;
        frame.flags = (byte)(endHeaders? ContinuationFrame.END_HEADERS : 0);
        frame.streamIdentifier = streamId;

        ContinuationFrame continuationFrame = new ContinuationFrame(encoder);
        continuationFrame.headers = convertHeadersToEntries(headers);

        frame.payloadLength = continuationFrame.length();
        frame.payload = continuationFrame;

        return frame;
    }

    public Http2Frame dataFrame(byte[] data, int streamId, boolean endStream){
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.DATA.value;
        frame.flags = endStream? DataFrame.END_STREAM : 0;
        frame.streamIdentifier = streamId;
        
        DataFrame dataFrame = new DataFrame();
        dataFrame.data = data;

        frame.payloadLength = dataFrame.length();
        frame.payload = dataFrame;

        return frame;
    }

    public Http2Frame goawayFrame(int errorCode, int lastStreamId){
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.GOAWAY.value;
        frame.streamIdentifier = lastStreamId;
        
        GoAwayFrame goAwayFrame = new GoAwayFrame();
        goAwayFrame.lastStreamId = lastStreamId;
        goAwayFrame.errorCode = errorCode;

        frame.payloadLength = goAwayFrame.length();
        frame.payload = goAwayFrame;

        return frame;
    }

    public Http2Frame windowUpdateFrame(int amount, int streamId){
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.WINDOW_UPDATE.value;
        frame.streamIdentifier = streamId;
        
        WindowUpdateFrame windowUpdateFrame = new WindowUpdateFrame();
        windowUpdateFrame.windowSizeIncrement = amount;

        frame.payloadLength = windowUpdateFrame.length();
        frame.payload = windowUpdateFrame;

        return frame;
    }

    public Http2Frame rstStreamFrame(int streamId, ErrorCodes errorCode){
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.RST_STREAM.value;
        frame.flags = 0;
        frame.streamIdentifier = streamId;

        RstStreamFrame rstFrame = new RstStreamFrame();
        rstFrame.errorCode = errorCode.value;

        frame.payloadLength = rstFrame.length();
        frame.payload = rstFrame;

        return frame;
    }
    public Http2Frame rstStreamFrame(int streamId){
        return rstStreamFrame(streamId, ErrorCodes.NO_ERROR);
    }

    public Http2Frame pingFrame(boolean ack){
        Http2Frame frame = new Http2Frame();
        frame.type = FrameTypes.PING.value;
        frame.flags = ack? PingFrame.ACK : 0;
        frame.streamIdentifier = 0;

        PingFrame pingFrame = new PingFrame();

        frame.payloadLength = pingFrame.length();
        frame.payload = pingFrame;

        return frame;
    }

    private List<SettingParameter> convertConfigToSettings(Http2Configuration config){
        List<SettingParameter> params = new ArrayList<>();
        
        if(Http2Configuration.DEFAULT_SETTINGS.getHeaderTableSize() != config.getHeaderTableSize())
        params.add(new SettingParameter(SettingsFrame.SETTINGS_HEADER_TABLE_SIZE, config.getHeaderTableSize()));

        if(Http2Configuration.DEFAULT_SETTINGS.getEnablePush() != config.getEnablePush())
        params.add(new SettingParameter(SettingsFrame.SETTINGS_ENABLE_PUSH, config.getEnablePush()));
        
        if(Http2Configuration.DEFAULT_SETTINGS.getMaxConcurrentStreams() != config.getMaxConcurrentStreams())
        params.add(new SettingParameter(SettingsFrame.SETTINGS_MAX_CONCURRENT_STREAMS, config.getMaxConcurrentStreams()));
        
        if(Http2Configuration.DEFAULT_SETTINGS.getInitialWindowSize() != config.getInitialWindowSize())
        params.add(new SettingParameter(SettingsFrame.SETTINGS_INITIAL_WINDOW_SIZE, config.getInitialWindowSize()));
        
        if(Http2Configuration.DEFAULT_SETTINGS.getMaxFrameSize() != config.getMaxFrameSize())
        params.add(new SettingParameter(SettingsFrame.SETTINGS_MAX_FRAME_SIZE, config.getMaxFrameSize()));
        
        if(Http2Configuration.DEFAULT_SETTINGS.getMaxHeaderListSize() != config.getMaxHeaderListSize())
        params.add(new SettingParameter(SettingsFrame.SETTINGS_MAX_HEADER_LIST_SIZE, config.getMaxHeaderListSize()));
        
        return params;
    }

    private List<Http2TableEntry> convertHeadersToEntries(HttpHeaders headers){
        List<Http2TableEntry> result = new ArrayList<>();
        for(Map.Entry<String,String> entry : headers.getHeaders().entrySet()){
            if(entry.getKey().equals("host")){
                result.add(new Http2TableEntry(":authority", entry.getValue()));
                continue;
            }
            result.add(new Http2TableEntry(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
