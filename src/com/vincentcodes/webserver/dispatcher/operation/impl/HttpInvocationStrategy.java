package com.vincentcodes.webserver.dispatcher.operation.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.annotaion.response.Mutatable;
import com.vincentcodes.webserver.component.header.EntityInfo;
import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.component.header.RangeHeader;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.HttpResponses;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.component.response.ResponseCodes;
import com.vincentcodes.webserver.dispatcher.HttpHandlerOptions;
import com.vincentcodes.webserver.dispatcher.HttpHandlerResult;
import com.vincentcodes.webserver.dispatcher.operation.MethodInvocationStrategy;
import com.vincentcodes.webserver.reflect.MethodDecorator;
import com.vincentcodes.webserver.util.FileExtUtils;

// TODO: response strategy?
public class HttpInvocationStrategy implements MethodInvocationStrategy {

    @Override
    public ResponseBuilder invoke(HttpRequest request, MethodDecorator method)
            throws InvocationTargetException, IOException, IllegalAccessException, IllegalArgumentException {
        ResponseBuilder response = HttpResponses.createDefault();
        if (method.parametersInFormOf(HttpRequest.class)) {
            if (method.returnsVoid()) {
                method.quickInvoke(request);
            } else {
                Object body = method.quickInvoke(request);
                return handleResponseBody(request, body);
            }
        } else if (method.parametersInFormOf(HttpRequest.class, ResponseBuilder.class)) {
            if (method.returnsVoid()) {
                method.quickInvoke(request, response);
            } else {
                Object body = method.quickInvoke(request, response);
                if (method.hasAnnotation(Mutatable.class)) {
                    ResponseBuilder realResponse = handleResponseBody(request, body);
                    // errors cannot be altered.
                    if (!ResponseCodes.isErrorResponseCode(realResponse.getResponseCode())) {
                        if (response.isResponseCodeModified())
                            realResponse.setResponseCode(response.getResponseCode());
                        realResponse.getHeaders().add(response.getHeaders());
                    }
                    return realResponse;
                } else {
                    return handleResponseBody(request, body);
                }
            }
        }
        return response;
    }

    /**
     * <p>
     * Default handler for a response. Features include (handling non-existent
     * files, file extensions, streaming service and response headers). In addition,
     * this method is only able to spit out these response codes: 200, 206, 404, 416. 
     * To handle this yourself, you need to VOID as return type and mutate the response 
     * yourself.
     * <p>
     * If you want your handler to return a File instance but NOTHING for something else,
     * what you can do is to return an Object, it's very ambiguous but it is a catch-all 
     * type.
     * <pre>{@code
     * &#64;HttpGet
     * &#64;Mutatable
     * &#64;RequestMapping("/authenticate")
     * public Object handleAuthorization(HttpRequest req, ResponseBuilder res){
     *   HttpHeaders headers = res.getHeaders();
     *   if(!req.getHeaders().hasHeader("authorization")){
     *     res.setResponseCode(401);
     *     headers.add("WWW-Authenticate", "Basic realm=\"Credentials are needed to access the website\"");
     *   }else{
     *     String credential = req.getHeaders().getHeader("authorization").replace("Basic", "").trim();
     *     String plaintext = new String(Base64.getDecoder().decode(credential));
     *     if(plaintext.equals("vincent:password")){
     *       return new File("readme.txt");
     *     }else{
     *       res.setResponseCode(403);
     *       // res.setResponseCode(401); // prompt the login panel again
     *     }
     *   }
     *   return "";
     * }
     * }</pre>
     * 
     * @param request 
     * @param body An object for Response Body ({@link File} / {@link Object})
     * @return a response which has its headers properly tinkered
     */
    private ResponseBuilder handleResponseBody(HttpRequest request, Object body) throws IOException, InvocationTargetException{
        if (body instanceof File) {
            return handleFile(request, (File)body, HttpHandlerOptions.empty());
        } else if(body instanceof HttpHandlerResult) {
            HttpHandlerResult result = (HttpHandlerResult) body;
            Object returnBody = result.getReturnResult();
            if(returnBody instanceof File)
                return handleFile(request, (File)returnBody, result.getOptions());
            
            // TODO: Options only work for File objects. Extend its capabilities
            return handleResponseBody(request, returnBody);
        } else if (body != null) {
            return HttpResponses.useObjectAsBody(body);
        }
        return HttpResponses.generate404Response();
    }
    private ResponseBuilder handleFile(HttpRequest request, File body, HttpHandlerOptions options) throws IOException{
        File file = (File) body;
        if (!file.exists() || file.isDirectory()) {
            return HttpResponses.generate404Response();
        }
        
        if(options.isAttachment()){
            return HttpResponses.useWholeFileAsBody(file, true);
        }

        HttpHeaders headers = request.getHeaders();
        EntityInfo info = headers.getEntityInfo();
        RangeHeader range = info.getRange();
        if (range != null && range.isValid()) {
            long startLoc = range.getRangeStart();
            long endLoc = range.getRangeEnd();

            if (range.hasRangeEnd()) {
                return HttpResponses.usePartialContent(file, startLoc, endLoc);
            } else {
                long totalSize = file.length();
                long defaultLength = startLoc + WebServer.MAX_PARTIAL_DATA_LENGTH;
                endLoc = defaultLength >= totalSize? totalSize-1 : defaultLength;
                return HttpResponses.usePartialContent(file, startLoc, endLoc);
            }
        }else{
            if(FileExtUtils.fileIsVideo(FileExtUtils.extractFileExtension(file.getName()))){
                long totalSize = file.length();
                int defaultLength = WebServer.MAX_PARTIAL_DATA_LENGTH;
                return HttpResponses.usePartialContent(file, 0, defaultLength >= totalSize? (int)totalSize-1 : defaultLength);
            }
            // if(headers.getHeader("accept-encoding").contains("gzip")){
            //     return HttpResponses.useWholeFileAsBody(file, EntityEncodings.GZIP);
            // }else if(headers.getHeader("accept-encoding").contains("deflate")){
            //     return HttpResponses.useWholeFileAsBody(file, EntityEncodings.DEFLATE);
            // }else{
            //     return HttpResponses.useWholeFileAsBody(file);
            // }
            return HttpResponses.useWholeFileAsBody(file);
        }
    }
}
