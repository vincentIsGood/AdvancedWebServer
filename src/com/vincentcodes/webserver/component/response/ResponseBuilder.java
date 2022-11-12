package com.vincentcodes.webserver.component.response;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.body.HttpBody;
import com.vincentcodes.webserver.component.body.HttpBodyStream;
import com.vincentcodes.webserver.component.header.HttpHeaders;

/**
 * This is a HTTP/1.1 response though you can convert it
 * into HTTP/2 response. See implementation.
 */
public class ResponseBuilder implements Closeable {
    private boolean responseCodeModified = false;
    // make the response empty, not recommended (unless you do not want to respond to a request)
    private boolean isEmptyResponse = false;
    private int responseCode;
    private String codeDesc;

    private HttpHeaders headers;
    private HttpBody body;

    private ResponseBuilder(int responseCode, String codeDesc, HttpHeaders headers, HttpBody body){
        this.responseCode = responseCode;
        this.codeDesc = codeDesc;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Creates a new ResponseBuilder which defaults to http version "HTTP/1.1".
     * Default headers (date, server) are added as well.
     * @param responseCode see {@link ResponseCodes} if you want to know what 
     * kind of response codes are supported
     * @param bodyType the body is used to store the data which will be sent to 
     * the browser
     */
    public static ResponseBuilder getInstance(int responseCode, HttpBody bodyType){
        if(bodyType == null)
            throw new IllegalArgumentException("Body type cannot be null");
        if(ResponseCodes.isResponseCodeSupported(responseCode)){
            ResponseBuilder res = new ResponseBuilder(responseCode, ResponseCodes.getCodeDescription(responseCode), getDefaulHeaders(), bodyType);
            return res;
        }
        throw new IllegalArgumentException("Bad Response Code");
    }

    /**
     * Creates a new ResponseBuilder which has an http version of "HTTP/1.1" and
     * a response code of 200. Default headers (date, server) are added as well.
     * @param bodyType the body is used to store the data which will be sent to 
     * the browser. Often, it's {@link HttpBodyStream}
     */
    public static ResponseBuilder getDefault(HttpBody bodyType){
        return getInstance(200, bodyType);
    }

    private static HttpHeaders getDefaulHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Date", WebServer.DATE_FORMAT.format(new Date()));
        headers.add("Server", "vws"); // vincent web server
        // headers.add("Cache-Control", "no-store");
        return headers;
    }

    /**
     * set response code
     * @param responseCode
     * @return whether the code is set successfully or not
     */
    public boolean setResponseCode(int responseCode){
        if(ResponseCodes.isResponseCodeSupported(responseCode)){
            this.responseCode = responseCode;
            this.codeDesc = ResponseCodes.getCodeDescription(responseCode);
            this.responseCodeModified = true;
        }
        return false;
    }

    public boolean isResponseCodeModified(){
        return responseCodeModified;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getCodeDesc() {
        return codeDesc;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpBody getBody() {
        return body;
    }

    public void setBody(HttpBody body){
        this.body = body;
    }

    /**
     * Resets the response but without headers
     * @param body set new body type
     */
    public void reset(HttpBody body){
        this.body = body;
    }

    /**
     * Restore the whole response with default headers
     * @param body
     */
    public void restore(HttpBody body){
        headers = getDefaulHeaders();
        this.body = body;
    }

    public void empty(){
        isEmptyResponse = true;
    }

    @Override
    public void close() throws IOException{
        if(body != null)
            body.close();
    }

    /**
     * Get the whole response in String. However, response body must be processed separately.
     * <p>
     * Here's a quote from the HTTP/1.1 spec about message headers:
     * <p>
     * The line terminator for message-header fields is the sequence CRLF. 
     * However, we recommend that applications, when parsing such headers, 
     * recognize a single LF as a line terminator and ignore the leading CR.
     * </p>
     * .NET System.Net.WebClient will throw a protocol violation if CRLF is
     * not used.
     * </p>
     */
    public String asString(){
        if(isEmptyResponse)
            return "";
        return this.toString();
    }

    /**
     * Default toString method.
     */
    public String toString(){
        StringBuilder builder = new StringBuilder();
        // String.format("%s %s %s\n")
        builder.append("HTTP/1.1 " + responseCode + " " + codeDesc + "\r\n");
        builder.append(headers.toString());
        builder.append("\r\n"); // end it. Next, Body (if present, done in WebServer)
        return builder.toString();
    }

}