package com.vincentcodes.webserver.http2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.vincentcodes.webserver.http2.constants.StreamState;
import com.vincentcodes.webserver.http2.types.DataFrame;
import com.vincentcodes.webserver.http2.types.HeadersFrame;
import com.vincentcodes.webserver.http2.types.RstStreamFrame;

/**
 * @see https://tools.ietf.org/html/rfc7540#section-8.1
 */
public class Http2Stream {
    private volatile StreamState state;
    private final int streamId; //readonly

    /**
     * Send WindowUpdateFrames to client's according to this value.
     * Allow them to keep sending stuff.
     */
    private int maxClientWindow;
    private int currentClientWindow;

    // The client will send us window update later anyways. Besides, safari will
    // not send window update after we have run out of server window.
    // private int currentServerWindow;

    private StreamIOHandler inputHandler;
    private StreamIOHandler outputHandler;
    private Http2FrameGenerator frameGenerator;
    private Http2RequestConverter converter;
    
    /**
     * By using this constructor you are required to manually setup the stream. Otherwise,
     * use {@link #Http2Stream(int, StreamIOHandler, StreamIOHandler, Http2FrameGenerator)}
     */
    public Http2Stream(int streamId){
        this.state = StreamState.IDLE;
        this.streamId = streamId;
    }

    public Http2Stream(int streamId, StreamIOHandler inputHandler, StreamIOHandler outputHandler, Http2FrameGenerator frameGenerator){
        this.state = StreamState.IDLE;
        this.streamId = streamId;
        this.maxClientWindow = frameGenerator.getConfig().getInitialWindowSize();
        this.currentClientWindow = maxClientWindow;
        this.inputHandler = inputHandler;
        this.outputHandler = outputHandler;
        this.frameGenerator = frameGenerator;
        this.converter = new Http2RequestConverter(frameGenerator);
    }

    public StreamState getState() {
        return state;
    }

    public int getStreamId() {
        return streamId;
    }

    public Http2FrameGenerator getFrameGenerator(){
        return frameGenerator;
    }

    /**
     * Get request converter associated to the stream
     */
    public Http2RequestConverter getConverter(){
        return converter;
    }

    /**
     * A handler which is used to receive frame and process them 
     * from connection. Processing may include converting frames 
     * into HttpRequest object and send them through the connection.
     * All happens within this handler.
     */
    public void setInputHandler(StreamIOHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    /**
     * A handler which is used to send frame through connection.
     */
    public void setOutputHandler(StreamIOHandler outputHandler) {
        this.outputHandler = outputHandler;
    }

    /**
     * Process frame coming from the connection and changes state of 
     * the stream automatically. Both IO happens here.
     * <p>
     * inputHandler must be set before calling this method
     */
    public void process(Http2Frame frame) throws IOException, InvocationTargetException{
        modifyClientWindow(frame);
        transitionState(frame, false);
        inputHandler.accept(this, frame);
    }

    /**
     * Send frame through connection and changes state of the stream
     * automatically. 
     * <p>
     * outputHandler must be set before calling this method.
     * @param frame [mutate] streamId will be set to the current 
     * object's streamId
     */
    public void send(Http2Frame frame) throws IOException, InvocationTargetException{
        frame.streamIdentifier = streamId;
        sendUnsafe(frame);
    }
    public void send(List<Http2Frame> frames) throws IOException, InvocationTargetException{
        for(Http2Frame frame : frames){
            send(frame);
        }
    }
    private void sendUnsafe(Http2Frame frame) throws IOException, InvocationTargetException{
        if(state != StreamState.CLOSED){
            // WebServer.logger.debug("Send: " + frame.toString());
            transitionState(frame, true);
            outputHandler.accept(this, frame);
        }
    }

    private void modifyClientWindow(Http2Frame frame) throws IOException, InvocationTargetException{
        if(frame.payload instanceof DataFrame){
            currentClientWindow -= frame.payloadLength;
        }
        while(currentClientWindow < maxClientWindow){
            send(frameGenerator.windowUpdateFrame(Http2Connection.WINDOW_UPDATE_AMOUNT, -1));
            sendUnsafe(frameGenerator.windowUpdateFrame(Http2Connection.WINDOW_UPDATE_AMOUNT, 0)); // You need to add window to stream 0 as well
            currentClientWindow += Http2Connection.WINDOW_UPDATE_AMOUNT;
        }
    }

    /**
     * Reserved states are not supported (to be implemented).
     * @param sending distinguish between sending / receiving (processing)
     * @see StreamState
     */
    private void transitionState(Http2Frame frame, boolean sending){
        // It is possible to idle -> open -> half closed in 1 frame
        if(state == StreamState.IDLE){
            if(frame.payload instanceof HeadersFrame){
                state = StreamState.OPEN;
            }
        }
        if(state == StreamState.OPEN){
            if((frame.flags & HeadersFrame.END_STREAM) > 0){
                if(sending){
                    state = StreamState.HALF_CLOSED_LOCAL;
                }else{
                    state = StreamState.HALF_CLOSED_REMOTE;
                }
            }
        }
        if(state == StreamState.HALF_CLOSED_REMOTE){
            if(sending){
                if((frame.flags & HeadersFrame.END_STREAM) > 0
                || frame.payload instanceof RstStreamFrame){
                    state = StreamState.CLOSED;
                }
            }else{
                if(frame.payload instanceof RstStreamFrame){
                    state = StreamState.CLOSED;
                }
            }
        }
        if(state == StreamState.HALF_CLOSED_LOCAL){
            if(sending){
                if(frame.payload instanceof RstStreamFrame){
                    state = StreamState.CLOSED;
                }
            }else{
                if((frame.flags & HeadersFrame.END_STREAM) > 0
                || frame.payload instanceof RstStreamFrame){
                    state = StreamState.CLOSED;
                }
            }
        }
    }

    public String toString(){
        return String.format("{Http2Stream state: '%s', streamId: %d}", state, streamId);
    }
}
