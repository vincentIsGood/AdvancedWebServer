package com.vincentcodes.webserver.component.header;

import java.util.HashMap;
import java.util.Map;

/**
 * Pseudo-headers (avilable in HTTP/2.0 ONLY) include:
 * :method    (eg. GET)
 * :path      (eg. /)
 * :scheme    (eg. http / https)
 * :authority (equivalent to "Host" header)
 * 
 * @see https://tools.ietf.org/html/rfc7540#section-8.1.2.3
 */
public class HttpHeaders {
    // All header keys are in lower case
    private HashMap<String, String> headers;
    
    public HttpHeaders(){
        this.headers = new HashMap<>();
    }

    /**
     * @param headers All headers are in lower case. Values are not constrained
     */
    public HttpHeaders(HashMap<String, String> headers){
        this.headers = headers;
    }

    public Map<String, String> getHeaders(){
        return headers;
    }

    /**
     * All keys are turned into lower case.
     * If the map previously contained a mapping for the key, the old value is replaced
     */
    public void add(String key, String value){
        key = key.toLowerCase();
        headers.put(key, value);
    }

    /**
     * @see #add(String, String)
     */
    public void add(HttpHeaders headers){
        headers.getHeaders().forEach((k, v) -> {
            // if(!this.headers.containsKey(k))
            this.headers.put(k, v);
        });
    }

    public boolean hasHeader(String key){
        return getHeader(key) != null;
    }

    /**
     * Get a header which you have previously added.
     * @param key Note: All header keys are in lower case (eg. content-length)
     * @return the http header value corresponding to the key or <code>null</code> if nothing is found.
     */
    public String getHeader(String key){
        return headers.get(key);
    }

    public int size(){
        return headers.size();
    }

    /**
     * Get common entity info (Info of the body of a request / response).
     * @return
     */
    public EntityInfo getEntityInfo(){
        return EntityInfo.create(
            getHeader("content-length"),
            getHeader("content-type"),
            getHeader("content-encoding"),
            getHeader("transfer-encoding"),
            getHeader("range"));
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        headers.forEach((k, v)->{
            builder.append(k + ": " + v + "\r\n");
        });
        return builder.toString();
    }

    // Utils
    /**
     * For example, 
     * <pre>
     * ...
     * Content-Disposition: form-data; name="myFile"; filename="foo.txt"
     * ...
     * </pre>
     * <pre>
     * ...
     * String value = "form-data; name=\"myFile\"; filename=\"foo.txt\""
     * assertEquals("form-data", extractDirectValue(value));
     * </pre>
     */
    public static String extractDirectValue(String headerValue){
        int semicolonPos = 0;
        if((semicolonPos = headerValue.indexOf(';')) != -1){
            return headerValue.substring(0, semicolonPos);
        }
        return headerValue;
    }
    /**
     * For example, 
     * <pre>
     * ...
     * Content-Disposition: form-data; name="myFile"; filename="foo.txt"
     * ...
     * </pre>
     * <pre>
     * ...
     * String value = "form-data; name=\"myFile\"; filename=\"foo.txt\""
     * assertEquals("myFile", extractParameter(value, "name"));
     * </pre>
     * @return null if nothing is found
     */
    public static String extractParameter(String headerValue, String key){
        int startingPoint = 0;
        if((startingPoint = headerValue.indexOf(key)) != -1){
            startingPoint = headerValue.indexOf('=', startingPoint)+1;
            if(headerValue.charAt(startingPoint) == '"'){
                return headerValue.substring(startingPoint+1, headerValue.indexOf('"', startingPoint+1));
            }else{
                int endingPoint = headerValue.indexOf(';', startingPoint);
                endingPoint = endingPoint == -1? headerValue.length() : endingPoint;
                return headerValue.substring(startingPoint, endingPoint);
            }
        }
        return null;
    }
}
