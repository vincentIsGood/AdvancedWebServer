package com.vincentcodes.webserver.http2;

import java.util.List;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.http2.types.SettingsFrame;

/**
 * Defines the settings. In practice, you need 1 for vws's
 * configuration and 1 for the client.
 * <p>
 * To find each of their IDs, see {@link SettingsFrame}
 */
public class Http2Configuration {
    private int SETTINGS_HEADER_TABLE_SIZE = 4096; // value default: 4096
    
    /**
     * This flag must not be sent from a server.
     */
    private int SETTINGS_ENABLE_PUSH = 1; // value default: 1 (enable)
    private int SETTINGS_MAX_CONCURRENT_STREAMS = WebServer.MAX_HTTP2_STREAMS_INDEX; // value default: unlimited
    
    /**
     * Indicates the sender's initial window size (in octets) for stream-level flow control.
     */
    private int SETTINGS_INITIAL_WINDOW_SIZE = 1024 * 1024; // value default: 2^16-1 (65535 bytes)
    
    /**
     * This controls how large the frames Clients can send. Not the Server itself.
     */
    private int SETTINGS_MAX_FRAME_SIZE = 16384; // value default: 2^14 (16384 bytes)
    
    private int SETTINGS_MAX_HEADER_LIST_SIZE = Integer.MAX_VALUE; // value default: unlimited

    public static final Http2Configuration DEFAULT_SETTINGS = new Http2Configuration(){
        {
            setHeaderTableSize(4096);
            setEnablePush(1);
            setMaxConcurrentStreams(Integer.MAX_VALUE);
            setInitialWindowSize(65535);
            setMaxFrameSize(16384);
            setMaxHeaderListSize(Integer.MAX_VALUE);
        }
    };

    public void setHeaderTableSize(int value){
        SETTINGS_HEADER_TABLE_SIZE = value;
    }
    public void setEnablePush(int value){
        SETTINGS_ENABLE_PUSH = value;
    }
    public void setMaxConcurrentStreams(int value){
        SETTINGS_MAX_CONCURRENT_STREAMS = value;
    }
    public void setInitialWindowSize(int value){
        SETTINGS_INITIAL_WINDOW_SIZE = value;
    }
    public void setMaxFrameSize(int value){
        SETTINGS_MAX_FRAME_SIZE = value;
    }
    public void setMaxHeaderListSize(int value){
        SETTINGS_MAX_HEADER_LIST_SIZE = value;
    }

    public int getHeaderTableSize(){
        return SETTINGS_HEADER_TABLE_SIZE;
    }
    public int getEnablePush(){
        return SETTINGS_ENABLE_PUSH;
    }
    public int getMaxConcurrentStreams(){
        return SETTINGS_MAX_CONCURRENT_STREAMS;
    }
    public int getInitialWindowSize(){
        return SETTINGS_INITIAL_WINDOW_SIZE;
    }
    public int getMaxFrameSize(){
        return SETTINGS_MAX_FRAME_SIZE;
    }
    public int getMaxHeaderListSize(){
        return SETTINGS_MAX_HEADER_LIST_SIZE;
    }

    public void apply(SettingsFrame settingsFrame){
        apply(settingsFrame.params);
    }
    public void apply(List<SettingParameter> params){
        for(SettingParameter param : params){
            apply(param.getIdentifier(), (int)param.getValue());
        }
    }
    public void apply(int identifier, int value){
        switch(identifier){
            case 1: SETTINGS_HEADER_TABLE_SIZE = value; break;
            case 2: SETTINGS_ENABLE_PUSH = value; break;
            case 3: SETTINGS_MAX_CONCURRENT_STREAMS = value; break;
            case 4: SETTINGS_INITIAL_WINDOW_SIZE = value; break;
            case 5: SETTINGS_MAX_FRAME_SIZE = value; break;
            case 6: SETTINGS_MAX_HEADER_LIST_SIZE = value; break;
        }
    }
}
