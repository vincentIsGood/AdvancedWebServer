package com.vincentcodes.webserver.component.request;

import java.util.HashMap;

/**
 * The first line of the request (including method, request path and http version)
 */
public class HttpRequestBasicInfo {
    private String method;
    private HttpRequestPath path;
    private String version;
    private HashMap<String, String> arguments;

    public HttpRequestBasicInfo(String method, String path, String version) {
        this.method = method;
        this.path = new HttpRequestPath(path);
        this.version = version;
        this.arguments = new HashMap<>(0);
    }

    public HttpRequestBasicInfo(String method, String path, String version, HashMap<String, String> arguments) {
        this(method, path, version);
        this.arguments = arguments;
    }

    public String getMethod() {
        return method;
    }

    public HttpRequestPath getPath() {
        return path;
    }

    /**
     * @return eg. HTTP/1.1 or HTTP/2.0
     */
    public String getVersion() {
        return version;
    }

    public boolean hasParameters(){
        return arguments.size() > 0;
    }

    /**
     * @return null if the key is not found
     */
    public String getParameter(String key){
        return arguments.get(key);
    }

    public HashMap<String, String> getParameters() {
        return arguments;
    }
    
}
