package com.vincentcodes.webserver.component.request;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.body.HttpBody;
import com.vincentcodes.webserver.component.header.HttpHeaders;

public class HttpRequest implements Closeable{
    public static final List<String> SUPPORTED_METHODS = WebServer.SUPPORTED_REQUEST_METHOD.keySet().stream().collect(Collectors.toList());

    private boolean isRequestValid = true;
    private HttpRequestBasicInfo basicInfo;
    private HttpHeaders headers;
    private HttpBody body;
    private MultipartFormData multipart;
    private String wholeRequest; // the body is not included (need to be set by others)

    /**
     * The start line is the first line of the request (including method, 
     * request path and http version)
     */
    public HttpRequestBasicInfo getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(HttpRequestBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    /**
     * Multipart form data do not have {@link HttpBody}.
     * Please check if it has Multipart data using 
     * {@link #hasMultipartData()}
     */
    public HttpBody getBody() {
        return body;
    }

    public void setBody(HttpBody body) {
        this.body = body;
    }

    public boolean hasMultipartData(){
        return multipart != null;
    }

    public void setMultipartFormData(MultipartFormData multipart){
        this.multipart = multipart;
    }

    public MultipartFormData getMultipartFormData(){
        return multipart;
    }

    public boolean isValid(){
        return isRequestValid;
    }
    
    /**
     * Used to indicate an HttpRequest is invalid. This method is used
     * in several classes.
     * 
     * @see com.vincentcodes.webserver.WebServer#start()
     * @see com.vincentcodes.webserver.component.request.RequestParser#parse(java.io.InputStream)
     */
    public void invalid(){
        this.isRequestValid = false;
    }

    /**
     * Get the original request in string.
     * @return null if it is an http2 connection
     */
    public String getWholeRequest() {
        return wholeRequest;
    }

    public void setWholeRequest(String wholeRequest) {
        this.wholeRequest = wholeRequest;
    }

    @Override
    public void close() throws IOException {
        try{
            if(body != null)
                body.close();
        }catch(IOException e){
            e.printStackTrace();
            throw e;
        }finally{
            if(multipart != null){
                for(String name : multipart.getNames()){
                    try{
                        multipart.getData(name).getBody().close();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public String toHttpString(){
        return toHttpString("HTTP/1.1", this);
    }
    public String toHttp2String(){
        return toHttpString("HTTP/2", this);
    }

    /**
     * excludes the "?"
     */
    private static String rebuildPathParameters(HttpRequestBasicInfo basicInfo) throws UnsupportedEncodingException{
        HashMap<String,String> params = basicInfo.getParameters();
        if(params.size() == 0){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        for(Map.Entry<String, String> entry : params.entrySet()){
            builder.append(URLEncoder.encode(entry.getKey(), "utf-8"))
                .append("=")
                .append(URLEncoder.encode(entry.getValue(), "utf-8")).append("&");
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }
    public static String rebuildPath(HttpRequestBasicInfo basicInfo) throws UnsupportedEncodingException{
        return URLEncoder.encode(basicInfo.getPath().get(), "utf-8").replaceAll("%2F", "/") + rebuildPathParameters(basicInfo);
    }
    public static String toHttpString(String version, HttpRequest request){
        StringBuilder builder = new StringBuilder();
        HttpRequestBasicInfo basicInfo = request.getBasicInfo();
        HttpHeaders headers = request.getHeaders();
        try{
            // eg. GET /asd.html HTTP/1.1
            builder.append(basicInfo.getMethod() + " " + rebuildPath(basicInfo) + " " + version + "\r\n");
            for(Map.Entry<String, String> entry : headers.getHeaders().entrySet()){
                builder.append(entry.getKey() + ": " + entry.getValue() + "\r\n");
            }
            builder.append("\r\n");
        }catch(Exception ignored){}
        return builder.toString();
    }
}
