package com.vincentcodes.webserver.component.response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.vincentcodes.webserver.component.body.HttpBody;
import com.vincentcodes.webserver.component.body.HttpBodyFileStream;
import com.vincentcodes.webserver.component.body.HttpBodyStream;
import com.vincentcodes.webserver.component.header.EntityEncodings;
import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.dispatcher.operation.DispatcherOperation;
import com.vincentcodes.webserver.util.FileExtUtils;

/**
 * This class returns common http responses. It is used in
 * implementations of {@link DispatcherOperation}.
 */
public class HttpResponses {
    public static ResponseBuilder createDefault(){
        return ResponseBuilder.getDefault(new HttpBodyStream());
    }
    
    public static ResponseBuilder createGenericResponse(int responseCode){
        ResponseBuilder response = ResponseBuilder.getInstance(responseCode, new HttpBodyStream());
        // response.getHeaders().add("connection", "close"); // HTTP/2 does not support this
        return response;
    }

    public static ResponseBuilder generate400Response() {
        return createGenericResponse(400);
    } 

    public static ResponseBuilder generate404Response() {
        return createGenericResponse(404);
    }

    public static ResponseBuilder generate416Response(long totalLength){
        ResponseBuilder response = ResponseBuilder.getInstance(416, new HttpBodyStream());
        response.getHeaders().add("content-range", "*/" + totalLength);
        // response.getHeaders().add("connection", "close"); // HTTP/2 does not support this
        return response;
    }

    /**
     * long is currently not supported.
     * @param startingByte 0 is fine
     */
    public static ResponseBuilder usePartialContent(File file, long startingByte, long endingByte) throws IOException{
        long fileTotalSize = file.length();
        if (startingByte < 0 || endingByte >= fileTotalSize || endingByte < startingByte) {
            return HttpResponses.generate416Response(fileTotalSize);
        }

        /**
         * According to MDN:
         * HTTP/1.1 206 Partial Content
         * Content-Range: bytes 0-1023/146515
         * Content-Length: 1024
         * 
         * ...
         * (binary content)
         */
        long contentLength = endingByte-startingByte+1;
        HttpBodyFileStream requestBody = new HttpBodyFileStream(file, contentLength);
        ResponseBuilder response = ResponseBuilder.getInstance(206, requestBody);
        HttpHeaders headers = response.getHeaders();

        requestBody.skip(startingByte);

        // TODO: Legacy Code
        // try(FileInputStream fis = new FileInputStream(file)){
        //     fis.skip(startingByte);

        //     long contentLength = endingByte-startingByte+1;
        //     if(contentLength > Integer.MAX_VALUE){
        //         WebServer.logger.warn("Requested payload size is larger than Integer.MAX_VALUE");
        //     }
        //     byte[] buffer = new byte[(int)contentLength];
        //     fis.read(buffer);
        //     requestBody.writeToBody(buffer);
        // }

        headers.add("accept-ranges", "bytes");
        headers.add("content-type", FileExtUtils.determineMimeType(file));
        // String.format("bytes %d-%d/%d")
        headers.add("content-range", "bytes " + startingByte + "-" + endingByte + "/" + fileTotalSize);
        headers.add("content-length", Long.toString(requestBody.length()));

        return response;
    }

    public static ResponseBuilder useWholeFileAsBody(File file) throws IOException {
        return useWholeFileAsBody(file, null, false);
    }
    public static ResponseBuilder useWholeFileAsBody(File file, boolean isAttachment) throws IOException {
        return useWholeFileAsBody(file, null, isAttachment);
    }
    /**
     * @param acceptEncoding Text files will often be compressed, if any acceptEncoding is specified.
     */
    public static ResponseBuilder useWholeFileAsBody(File file, EntityEncodings acceptEncoding, boolean isAttachment) throws IOException {
        HttpBody requestBody = null;
        ResponseBuilder response = null;
        boolean isCommonTextFile = FileExtUtils.isCommonTextFile(FileExtUtils.extractFileExtension(file.getName()));
        if(isCommonTextFile){
            requestBody = new HttpBodyStream(acceptEncoding);
            response = ResponseBuilder.getDefault(requestBody);

            try(FileInputStream fis = new FileInputStream(file)){
                byte[] buffer = new byte[4096];
                int bytesRead;
                while((bytesRead = fis.read(buffer)) != -1){
                    requestBody.writeToBody(buffer, bytesRead);
                }
            }
        }else{
            requestBody = new HttpBodyFileStream(file, file.length());
            response = ResponseBuilder.getDefault(requestBody);
        }
        
        HttpHeaders headers = response.getHeaders();
        EntityEncodings acceptedEncoding = requestBody.getAcceptedEncoding();
        if(acceptEncoding != null 
        && (acceptedEncoding == EntityEncodings.GZIP || acceptedEncoding == EntityEncodings.DEFLATE)){
            headers.add("content-encoding", acceptEncoding.value());
        }

        if(!headers.hasHeader("content-type")){
            String contentType = FileExtUtils.determineMimeType(file);
            if(contentType.startsWith("text/")){
                contentType += "; charset=utf-8"; // default file-encoding is utf-8, so whatever.
            }
            headers.add("content-type", contentType);
        }
        headers.add("content-length", Long.toString(requestBody.length()));

        if(isAttachment){
            headers.add("cache-control", "no-store");
            headers.add("content-disposition", "attachment; filename=" + file.getName());
        }

        return response;
    }

    /**
     * Convert object to string as the response body.
     */
    public static ResponseBuilder useObjectAsBody(Object body) throws IOException{
        HttpBody requestBody = new HttpBodyStream();
        ResponseBuilder response = ResponseBuilder.getDefault(requestBody);
        String content = body.toString();
        HttpHeaders headers = response.getHeaders();

        if(content.length() > 0){
            if (!headers.hasHeader("content-type"))
                headers.add("content-type", "text/plain");
            
            byte[] bytesPayload = content.getBytes();
            headers.add("content-length", Integer.toString(bytesPayload.length));
            
            requestBody.writeToBody(bytesPayload);
        }
        return response;
    }

    /**
     * Convert object to string as the response body.
     */
    public static ResponseBuilder useStringAsJsonBody(String body) throws IOException{
        HttpBody requestBody = new HttpBodyStream();
        ResponseBuilder response = ResponseBuilder.getDefault(requestBody);
        HttpHeaders headers = response.getHeaders();

        if(body.length() > 0){
            if(headers.getHeader("content-type") == null)
                headers.add("content-type", "application/json");
            
            byte[] bytesPayload = body.getBytes(StandardCharsets.UTF_8);
            headers.add("content-length", Integer.toString(bytesPayload.length));
            
            requestBody.writeToBody(bytesPayload);
        }
        return response;
    }
}
