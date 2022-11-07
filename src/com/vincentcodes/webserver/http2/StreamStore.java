package com.vincentcodes.webserver.http2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vincentcodes.webserver.WebServer;

/**
 * Stores a bunch of streams in order
 */
public class StreamStore {
    private static final int MAX_CAP_INDEX = WebServer.MAX_HTTP2_STREAMS_INDEX;
    private List<Http2Stream> streams;

    public StreamStore(){
        streams = new ArrayList<>();
    }

    /**
     * @return empty there is none
     */
    public Optional<Http2Stream> findStream(int id){
        for(Http2Stream stream : streams){
            if(stream.getStreamId() == id)
                return Optional.of(stream);
        }
        return Optional.empty();
    }

    /**
     * @return empty there is none
     */
    public Optional<Http2Stream> findStream(Http2Frame frame){
        return findStream(frame.streamIdentifier);
    }

    public Http2Stream get(int i){
        return streams.get(i);
    }

    /**
     * @param stream new stream
     * @throws ArrayIndexOutOfBoundsException when list size > MAX capacity 
     * @throws IllegalArgumentException new stream must have a larger stream id
     */
    public void addStream(Http2Stream stream){
        checkBounds();
        checkIndex(stream);
        streams.add(stream);
    }
    
    private void checkBounds(){
        if(streams.size() > MAX_CAP_INDEX)
            throw new ArrayIndexOutOfBoundsException("Cannot add more streams");
    }

    private void checkIndex(Http2Stream newStream){
        if(streams.size() > 0){
            boolean newStreamHasLargerId = newStream.getStreamId() > streams.get(streams.size()-1).getStreamId();
            if(!newStreamHasLargerId){
                throw new IllegalArgumentException("New stream must have a larger stream id");
            }
        }
    }
}
