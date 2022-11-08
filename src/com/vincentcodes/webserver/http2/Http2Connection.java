package com.vincentcodes.webserver.http2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.HttpRequestValidator;
import com.vincentcodes.webserver.component.response.HttpResponses;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.exception.CannotParseRequestException;
import com.vincentcodes.webserver.helper.IOContainer;
import com.vincentcodes.webserver.http2.constants.ErrorCodes;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;
import com.vincentcodes.webserver.http2.types.GoAwayFrame;
import com.vincentcodes.webserver.http2.types.PingFrame;
import com.vincentcodes.webserver.http2.types.PriorityFrame;
import com.vincentcodes.webserver.http2.types.SettingsFrame;

// TODO: stream dependency, errors, are still not implemented yet.
/**
 * The main connection for http2.
 */
public class Http2Connection {
    public static final int WINDOW_UPDATE_AMOUNT = 32768;

    private IOContainer ioContainer;
    private Socket connection;
    private StreamStore streamStore;

    private HttpRequestValidator requestValidator;
    private HttpRequestDispatcher requestDispatcher;

    private Http2Configuration config;
    private Http2RequestParser http2Parser;
    private Http2FrameGenerator frameGenerator;

    private OutputStream os;
    private InputStream is;

    private boolean pingSent = false;

    public Http2Connection(IOContainer ioContainer, HttpRequestValidator requestValidator, HttpRequestDispatcher requestDispatcher){
        this.ioContainer = ioContainer;
        this.connection = ioContainer.getSocket();
        this.streamStore = new StreamStore();
        this.requestValidator = requestValidator;
        this.requestDispatcher = requestDispatcher;
    }

    /**
     * Setup http2 stuff.
     */
    public void setup(){
        is = ioContainer.getInputStream();
        os = ioContainer.getOutputStream();
        config = new Http2Configuration();
        
        // "the request and response dynamic tables are separate."
        // https://tools.ietf.org/html/rfc7541#section-2.2
        HpackDecoder decoder = new HpackDecoder(new DynamicTable(config, StaticTable.instance.size()+1));
        HpackEncoder encoder = new HpackEncoder(new DynamicTable(config, StaticTable.instance.size()+1));
        
        frameGenerator = new Http2FrameGenerator(encoder, config);
        http2Parser = new Http2RequestParser(decoder, config);
    }

    /**
     * @see Http2Stream#process()
     */
    private void universalInputHandler(Http2Stream stream, Http2Frame frame) throws IOException, InvocationTargetException{
        Http2RequestConverter converter = stream.getConverter();
        if(converter.isHttpMessageFrame(frame)){
            converter.addFrame(frame);

            Optional<HttpRequest> optRequest = converter.toRequest();
            if(optRequest.isPresent()){
                try(ResponseBuilder response = handleHttpRequest(optRequest.get())){
                    List<Http2Frame> frames = converter.fromResponse(response);
                    // max frame count is used to prevent safari from requesting a humongous payload
                    int maxFrameAmount = (int)Math.floor((WebServer.MAX_PARTIAL_DATA_LENGTH+1)/config.getMaxFrameSize()) + Http2RequestConverter.getNonDataFrameCount(frames);
                    if(frames.size() <= maxFrameAmount){
                        stream.send(frames);
                    }else{
                        // just end the stream halfway (just like how safari treat me)
                        frames.add(maxFrameAmount+1, stream.getFrameGenerator().rstStreamFrame(-1, ErrorCodes.CANCEL));
                        stream.send(frames);
                    }
                }
            }
        }else{
            if(frame.payload instanceof SettingsFrame){
                if((frame.flags & SettingsFrame.ACK) == 0){
                    SettingsFrame settings = (SettingsFrame)frame.payload;
                    config.apply(settings);
                    stream.send(frameGenerator.settingsFrameAck(-1));
                }
            }else if(frame.payload instanceof PriorityFrame){
                // Not implemented, very complex (involves stream dependency stuff)
            }else if(frame.payload instanceof PingFrame){
                if((frame.flags & PingFrame.ACK) == 0){
                    stream.send(frameGenerator.pingFrame(true));
                }else{
                    pingReceived();
                }
            }else if(frame.payload instanceof GoAwayFrame){
                connection.close();
            }
        }
    }

    /**
     * @see Http2Stream#send()
     */
    private void universalOutputHandler(Http2Stream stream, Http2Frame frame) throws IOException, InvocationTargetException{
        os.write(frame.toBytes());
    }

    /**
     * Let the connection object to take over. The whole 
     * http2 communication starts here. This is a 
     * blocking operation until the connection is closed.
     * <p>
     * Make sure {@link #setup()} is already called.
     */
    public void takeover() throws IOException, CannotParseRequestException, InvocationTargetException{
        streamStore.addStream(new Http2Stream(0, this::universalInputHandler, this::universalOutputHandler, frameGenerator));
        streamStore.get(0).send(frameGenerator.settingsFrame(0));
        initConnectionChecker();
        while(isConnected()){
            Http2Frame requestFrame = http2Parser.parse(is);
            // if(!(response.payload instanceof WindowUpdateFrame))
            //     WebServer.logger.warn("Recv: " + response.toString());
            // WebServer.logger.debug(requestFrame.toString());

            Optional<Http2Stream> optStream = streamStore.findStream(requestFrame);
            if(optStream.isPresent()){
                Http2Stream stream = optStream.get();
                stream.process(requestFrame);
            }else{
                Http2Stream newStream = new Http2Stream(requestFrame.streamIdentifier, this::universalInputHandler, this::universalOutputHandler, frameGenerator);
                streamStore.addStream(newStream);
                newStream.process(requestFrame);
            }
        }
    }

    private ResponseBuilder handleHttpRequest(HttpRequest request) throws InvocationTargetException{
        if(!requestValidator.requestIsValid(request)){
            request.invalid();
        }

        if(request.isValid()){
            return requestDispatcher.dispatchObjectToHandlers(request);
        }
        return HttpResponses.generate400Response();
    }

    public Socket getConnection() {
        return connection;
    }

    public StreamStore getStreams() {
        return streamStore;
    }

    public boolean isConnected(){
        return !(connection.isInputShutdown() || connection.isOutputShutdown());
    }
    
    private void initConnectionChecker(){
        new Thread("Http2 Connection Checker"){
            public void run(){
                try{
                    Socket socket = ioContainer.getSocket();
                    while(!socket.isClosed()){
                        if(pingSent) socket.close();
                        sendPing();
                        Thread.sleep(WebServer.WEBSOCKET_PING_INTERVAL_MILSEC);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                return;
            }
        }.start();
    }

    private void sendPing() throws IOException, InvocationTargetException{
        universalOutputHandler(streamStore.findStream(0).get(), frameGenerator.pingFrame(false));
        pingSent = true;
    }

    private void pingReceived(){
        pingSent = false;
    }
}
