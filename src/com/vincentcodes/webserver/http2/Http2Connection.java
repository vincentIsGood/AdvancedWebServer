package com.vincentcodes.webserver.http2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vincentcodes.net.UpgradableSocket;
import com.vincentcodes.webserver.ServerThreadUtils;
import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.HttpRequestValidator;
import com.vincentcodes.webserver.component.response.HttpResponses;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.exception.CannotParseRequestException;
import com.vincentcodes.webserver.helper.IOContainer;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.http2.hpack.HpackEncoder;
import com.vincentcodes.webserver.http2.types.GoAwayFrame;
import com.vincentcodes.webserver.http2.types.PingFrame;
import com.vincentcodes.webserver.http2.types.PriorityFrame;
import com.vincentcodes.webserver.http2.types.SettingsFrame;
import com.vincentcodes.webserver.http2.types.WindowUpdateFrame;

// TODO: stream dependency, errors, are still not implemented yet.
/**
 * The main connection for http2.
 */
public class Http2Connection {
    public static final int WINDOW_UPDATE_AMOUNT = 32768;

    private IOContainer ioContainer;
    private UpgradableSocket connection;
    private StreamStore streamStore;
    private Set<Http2Stream> busyStreams;

    private HttpRequestValidator requestValidator;
    private HttpRequestDispatcher requestDispatcher;

    private Http2Configuration config;
    private Http2RequestParser http2Parser;
    private Http2FrameGenerator frameGenerator;

    private ExecutorService executorService;

    private OutputStream os;
    private InputStream is;

    private boolean pingSent = false;

    public Http2Connection(IOContainer ioContainer, HttpRequestValidator requestValidator, HttpRequestDispatcher requestDispatcher){
        this.ioContainer = ioContainer;
        this.connection = ioContainer.getSocket();
        this.streamStore = new StreamStore();
        busyStreams = new HashSet<>();
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
     * @throws InterruptedException
     * @see Http2Stream#processQueuedUpFrames()
     */
    private void universalInputHandler(Http2Stream stream, Http2Frame frame) throws IOException, InvocationTargetException{
        if(WebServer.lowLevelDebugMode && !(frame.payload instanceof WindowUpdateFrame))
            WebServer.logger.debug("Recv: " + frame.toString());

        Http2RequestConverter converter = stream.getConverter();
        
        if(converter.isHttpMessageFrame(frame)){
            converter.addFrame(frame);

            Optional<HttpRequest> optRequest = converter.toRequest();
            if(optRequest.isPresent()){
                try(HttpRequest req = optRequest.get()){
                    req.setSocket(this.ioContainer);

                    WebServer.logger.debug(req.toHttp2String());
                    ResponseBuilder response = handleHttpRequest(req);
                    
                    if(response.getHeaders().getHeader("X-Vws-Raw-Tunnel") != null){
                        try {
                            ServerThreadUtils.socketTunnelInitialization(req, response, (remoteServerRes, os)->{
                                try {
                                    converter.streamResponseToStream(remoteServerRes, -1, stream);
                                } catch (InvocationTargetException | IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    
                    converter.streamResponseToStream(response, -1, stream);
                }
            }
            return;
        }
        
        if(frame.payload instanceof SettingsFrame){
            if((frame.flags & SettingsFrame.ACK) == 0){
                SettingsFrame settings = (SettingsFrame)frame.payload;
                config.apply(settings);
                stream.send(frameGenerator.settingsFrameAck(-1));
            }
        }else if(frame.payload instanceof WindowUpdateFrame){
            stream.modifyServerWindow((WindowUpdateFrame)frame.payload);
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
        stream.sendQueuedUpFrames();
    }

    /**
     * @see Http2Stream#send()
     */
    private void universalOutputHandler(Http2Stream stream, Http2Frame frame) throws IOException, InvocationTargetException{
        if(WebServer.lowLevelDebugMode && !(frame.payload instanceof WindowUpdateFrame))
            WebServer.logger.debug("Send: " + frame.toString());
        
        frame.streamBytesTo(os);
        // os.write(frame.toBytes());
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

        executorService = Executors.newFixedThreadPool(WebServer.HTTP2_HANDLER_THREADS);
        while(isConnected()){
            Http2Frame requestFrame = http2Parser.parse(is);
            Http2Stream stream = findCorrespondingStream(requestFrame);
            stream.queueUpClientFrames(requestFrame);
            
            // TODO: Bug fixed regarding race condition, but still need more testing
            if(!busyStreams.contains(stream)){
                busyStreams.add(stream);
                executorService.submit(() -> {
                    try {
                        stream.processQueuedUpFrames();
                        busyStreams.remove(stream);
                    } catch (InvocationTargetException | IOException e) {
                        if(WebServer.lowLevelDebugMode)
                            e.printStackTrace();
                    }
                });
            }
        }
    }

    private Http2Stream findCorrespondingStream(Http2Frame requestFrame) throws InvocationTargetException, IOException{
        Optional<Http2Stream> optStream = streamStore.findStream(requestFrame);
        Http2Stream stream;
        if(optStream.isPresent()){
            stream = optStream.get();
        }else{
            stream = new Http2Stream(requestFrame.streamIdentifier, this::universalInputHandler, this::universalOutputHandler, frameGenerator);
            streamStore.addStream(stream);
        }
        return stream;
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

    public UpgradableSocket getConnection() {
        return connection;
    }

    public StreamStore getStreams() {
        return streamStore;
    }

    public boolean isConnected(){
        Socket conn = connection.getUnderlyingSocket();
        return !(conn.isInputShutdown() || conn.isOutputShutdown());
    }
    
    private Thread initConnectionChecker(){
        Thread connectionChecker = new Thread("Http2 Connection Checker"){
            public void run(){
                try{
                    UpgradableSocket socket = ioContainer.getSocket();
                    while(!socket.isClosed()){
                        Thread.sleep(WebServer.WEBSOCKET_PING_INTERVAL_MILSEC);
                        if(pingSent) socket.close();
                        sendPing();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                return;
            }
        };
        connectionChecker.setDaemon(true);
        connectionChecker.start();
        return connectionChecker;
    }

    private void sendPing() throws IOException, InvocationTargetException{
        universalOutputHandler(streamStore.findStream(0).get(), frameGenerator.pingFrame(false));
        pingSent = true;
    }

    private void pingReceived(){
        pingSent = false;
    }
}
