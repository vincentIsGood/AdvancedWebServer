package com.vincentcodes.webserver.http2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.http2.constants.StreamState;
import com.vincentcodes.webserver.http2.types.DataFrame;
import com.vincentcodes.webserver.http2.types.HeadersFrame;
import com.vincentcodes.webserver.http2.types.RstStreamFrame;
import com.vincentcodes.webserver.http2.types.WindowUpdateFrame;

/**
 * @see https://tools.ietf.org/html/rfc7540#section-8.1
 */
public class Http2Stream {
    private volatile StreamState state;
    private final int streamId; //readonly

    /**
     * Send WindowUpdateFrames to client's according to this value.
     * Allow them to keep sending stuff.
     * 
     * This value controls how much CLIENT can send to the server.
     * eg. control client's uploading speed to server.
     */
    private int maxClientWindow;
    private int currentClientWindow;

    /**
     * I configured the server to take the same config as client.
     * 
     * maxServerWindow is not useful: since 0 is the boundary now.
     * 
     * This value controls how much SERVER can send back to client.
     * eg. control client's downloading speed from server.
     */
    // private int maxServerWindow;
    private int currentServerWindow;
    private Queue<Http2Frame> framesToBeSent;
    private Queue<Http2Frame> framesToBeRead;

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
        // this.maxServerWindow = maxClientWindow;
        this.currentClientWindow = maxClientWindow;
        this.currentServerWindow = maxClientWindow;
        this.inputHandler = inputHandler;
        this.outputHandler = outputHandler;
        this.frameGenerator = frameGenerator;
        this.converter = new Http2RequestConverter(frameGenerator);

        framesToBeSent = new ConcurrentLinkedQueue<>();
        framesToBeRead = new ConcurrentLinkedQueue<>();
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

    public void processQueuedUpFrames() throws IOException, InvocationTargetException{
        while(framesToBeRead.size() > 0){
            process(framesToBeRead.poll());
        }
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
    public void queueUpClientFrames(Http2Frame frame){
        framesToBeRead.add(frame);
    }

    /**
     * Send frame through connection and changes state of the stream
     * automatically. 
     * <p>
     * outputHandler must be set before calling this method.
     * @param frame [mutate] streamId will be set to the current 
     * object's streamId
     * @throws IOException when errors occur while sending the bytes 
     * under the low level layers OR when the stream is in 
     * {@link StreamState#CLOSED} state.
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

    /**
     * Currently has NO effect
     */
    public void sendQueuedUpFrames() throws InvocationTargetException, IOException{
        while(framesToBeSent.size() > 0 && currentServerWindow > 0){
            sendUnsafe(framesToBeSent.poll());
        }
    }
    /**
     * Will queue up frames if server's window is not enough.
     * Will resend them AS SOON AS WindowUpdate is received.
     */
    private void sendUnsafe(Http2Frame frame) throws IOException, InvocationTargetException{
        // Still in Test stage: To make server side respect client's window: uncomment the following
        // if(currentServerWindow < 0){
        //     framesToBeSent.add(frame);
        //     return;
        // }
        // if(frame.payload instanceof DataFrame){
        //     currentServerWindow -= frame.payloadLength;
        // }
        if(state != StreamState.CLOSED){
            transitionState(frame, true);
            outputHandler.accept(this, frame);
        }else if(WebServer.THROW_ERROR_WHEN_SEND_ON_CLOSED){
            throw new IOException("Http2Stream is closed: " + toString());
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
    public void modifyServerWindow(WindowUpdateFrame windowUpdateFrame){
        currentServerWindow += windowUpdateFrame.windowSizeIncrement;
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
