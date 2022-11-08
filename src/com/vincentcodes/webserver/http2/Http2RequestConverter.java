package com.vincentcodes.webserver.http2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.vincentcodes.webserver.component.body.HttpBodyStream;
import com.vincentcodes.webserver.component.header.EntityInfo;
import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.HttpRequestBasicInfo;
import com.vincentcodes.webserver.component.request.MultipartFormData;
import com.vincentcodes.webserver.component.request.RequestParser;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.helper.TextBinaryInputStream;
import com.vincentcodes.webserver.http2.constants.FrameTypes;
import com.vincentcodes.webserver.http2.errors.StreamError;
import com.vincentcodes.webserver.http2.types.ContinuationFrame;
import com.vincentcodes.webserver.http2.types.DataFrame;
import com.vincentcodes.webserver.http2.types.HeadersFrame;

/**
 * Used to convert a combination of specific {@link Http2Frame} 
 * of the same stream into an {@link HttpRequest} object.
 */
public class Http2RequestConverter {
    private LinkedList<Http2Frame> buffer;
    private Http2FrameGenerator frameGenerator;
    private Http2Configuration config;

    public Http2RequestConverter(Http2FrameGenerator frameGenerator){
        buffer = new LinkedList<>();
        this.frameGenerator = frameGenerator;
        this.config = frameGenerator.getConfig();
    }

    public Http2FrameGenerator getFrameGenerator(){
        return frameGenerator;
    }

    /**
     * Frame gets added if the reserved size is
     * larger than 0
     */
    public void addFrame(Http2Frame frame){
        if(!isHttpMessageFrame(frame)){
            throw new IllegalArgumentException("Cannot pass frame of type '" + frame.payload.getClass() + "' into converter");
        }
        buffer.add(frame);
    }

    public boolean isHttpMessageFrame(Http2Frame frame){
        // null payload is possible for DATA frame
        if(frame.type == FrameTypes.HEADERS.value
        || frame.type == FrameTypes.CONTINUATION.value
        || frame.type == FrameTypes.DATA.value){
            return true;
        }
        return false;
    }

    /**
     * If the conversion is successful, an HttpRequest object 
     * is contained inside the Optional return object.
     * @return empty if an HttpRequest cannot be converted from 
     * existing frames.
     */
    public Optional<HttpRequest> toRequest(){
        if(buffer.size() == 0){
            return Optional.empty();
        }else if(buffer.size() == 1){
            Http2Frame firstFrame = buffer.getFirst();
            if(firstFrame.payload instanceof HeadersFrame){
                HeadersFrame payload = (HeadersFrame)firstFrame.payload;
                if(hasEndHeadersFlag(firstFrame) && hasEndStreamFlag(firstFrame)){
                    HttpRequest request = new HttpRequest();
                    request.setBasicInfo(extractBasicInfo(payload));
                    request.setHeaders(extractHeaders(payload));
                    reset();
                    return Optional.of(request);
                }
            }else{
                throw new StreamError("Cannot receive other frame without Headers frame");
            }
        }else{
            Http2Frame lastFrame = buffer.getLast();
            // ContinuationFrame is allowed to be sent after a HeadersFrame with END_STREAM flag set
            if(!(lastFrame.payload instanceof ContinuationFrame) && !hasEndStreamFlag(lastFrame)){
                return Optional.empty();
            }
            if(lastFrame.payload instanceof ContinuationFrame){
                Optional<Http2Frame> optHeadersFrame = getHeadersFrameFromBuffer();
                if(!optHeadersFrame.isPresent()){
                    throw new StreamError("Cannot receive Continuation frame without Headers frame");
                }
                if(!hasEndHeadersFlag(lastFrame) || !hasEndStreamFlag(optHeadersFrame.get())){
                    return Optional.empty();
                }
            }

            HttpRequest request = new HttpRequest();
            HttpHeaders headers = new HttpHeaders();
            HttpBodyStream body = new HttpBodyStream();
            request.setHeaders(headers);
            request.setBody(body);

            for(Http2Frame currentFrame : buffer){
                if(currentFrame.payload instanceof HeadersFrame){
                    HeadersFrame payload = (HeadersFrame)currentFrame.payload;
                    request.setBasicInfo(extractBasicInfo(payload));
                    headers.add(extractHeaders(payload));
                }else if(currentFrame.payload instanceof ContinuationFrame){
                    ContinuationFrame payload = (ContinuationFrame)currentFrame.payload;
                    headers.add(extractHeaders(payload));
                }else if(currentFrame.payload instanceof DataFrame){
                    DataFrame payload = (DataFrame)currentFrame.payload;
                    try{
                        body.writeToBody(payload.data);
                    }catch(IOException ignored){}
                }
                // skip the null payload frames
            }
            try{
                EntityInfo entityInfo = request.getHeaders().getEntityInfo();
                if(entityInfo.getType().equals("multipart/form-data")){
                    TextBinaryInputStream tbis = new TextBinaryInputStream(new ByteArrayInputStream(request.getBody().getBytes()));
                    MultipartFormData multipart = RequestParser.parseMultipartFormData(tbis, entityInfo, new StringBuilder());
                    request.setMultipartFormData(multipart);
                    request.setBody(null); // multipart form data has no body (see HttpRequest)
                }
            }catch(IOException ignored){}
            reset();
            return Optional.of(request);
        }
        return Optional.empty();
    }

    public List<Http2Frame> fromResponse(ResponseBuilder response){
        List<Http2Frame> frames = new ArrayList<>();
        if(response.getHeaders().getEntityInfo().getLength() > 0){
            frames.add(frameGenerator.responseHeadersFrame(response.getResponseCode(), response.getHeaders(), -1, true, false));
            // Make sure we send at least 10000 bytes to keep up with the streaming service.
            int dataSize = config.getMaxFrameSize()/2 < 10000? config.getMaxFrameSize() : config.getMaxFrameSize()/2;
            byte[] data;
            boolean endOfStream = false;
            while(!endOfStream){
                data = response.getBody().getBytes(dataSize);
                endOfStream = data.length != dataSize;
                frames.add(frameGenerator.dataFrame(data, -1, endOfStream));
            }
            // byte[] resBody = response.getBody().getBytes();
            // int nextIndex = 0;
            // for(; nextIndex < resBody.length - dataSize; nextIndex += dataSize){
            //     frames.add(frameGenerator.dataFrame(Arrays.copyOfRange(resBody, nextIndex, nextIndex + dataSize), -1, false));
            // }
            // frames.add(frameGenerator.dataFrame(Arrays.copyOfRange(resBody, nextIndex, resBody.length), -1, true));
            // frames.add(frameGenerator.rstStreamFrame(-1));
        }else if(response.getHeaders().size() > 0){
            frames.add(frameGenerator.responseHeadersFrame(response.getResponseCode(), response.getHeaders(), -1, true, true));
        }else{
            frames.add(frameGenerator.responseHeadersFrame(response.getResponseCode(), -1, true, true));
            // frames.add(frameGenerator.rstStreamFrame(-1));
        }
        return frames;
    }

    /**
     * Clear the buffer
     */
    public void reset(){
        buffer.clear();
    }

    public static int getNonDataFrameCount(List<Http2Frame> arr){
        int count = 0;
        for(Http2Frame frame : arr)
            if(frame.type != FrameTypes.DATA.value) 
                count++;
        return count;
    }

    private Optional<Http2Frame> getHeadersFrameFromBuffer(){
        for(Http2Frame currentFrame : buffer){
            if(currentFrame.payload instanceof HeadersFrame){
                return Optional.of(currentFrame);
            }
        }
        return Optional.empty();
    }

    private boolean hasEndStreamFlag(Http2Frame frame){
        // HeadersFrame.END_STREAM is the same as DataFrame.END_STREAM (bit 0)
        return (frame.flags & HeadersFrame.END_STREAM) > 0;
    }
    private boolean hasEndHeadersFlag(Http2Frame frame){
        // HeadersFrame.END_HEADERS is the same as others (bit 2)
        return (frame.flags & HeadersFrame.END_HEADERS) > 0;
    }

    private HttpRequestBasicInfo extractBasicInfo(HeadersFrame payload){
        List<Http2TableEntry> headers = payload.headers;
        String method = null;
        String path = null;
        for(Http2TableEntry entry : headers){
            if(entry.getName().equals(":method")){
                method = entry.getValue();
            }else if(entry.getName().equals(":path")){
                path = entry.getValue();
            }
            if(method != null && path != null)
                break;
        }
        if(method == null || path == null)
            return null;
        try{
            return RequestParser.parseFirstLine(method + " " + path + " HTTP/1.1"); // fake 1.1
        }catch(Exception ignored){}
        return null;
    }

    private HttpHeaders extractHeaders(HeadersFrame payload){
        return extractHeaders(payload.headers);
    }

    private HttpHeaders extractHeaders(ContinuationFrame payload){
        return extractHeaders(payload.headers);
    }

    private HttpHeaders extractHeaders(List<Http2TableEntry> headers){
        HttpHeaders newHeaders = new HttpHeaders();
        for(Http2TableEntry entry : headers){
            if(!entry.getName().equals(":method")
            && !entry.getName().equals(":path")
            && !entry.getName().equals(":scheme")){
                if(entry.getName().equals(":authority")){
                    newHeaders.add("host", entry.getValue());
                    continue;
                }
                newHeaders.add(entry.getName(), entry.getValue());
            }
        }
        return newHeaders;
    }
}
