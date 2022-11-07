package com.vincentcodes.webserver.component.request;

import com.vincentcodes.webserver.component.header.HttpHeaders;

/**
 * <p>
 * Used to validate an {@link HttpRequest}. It is recommended to configure
 * it according to the standard of HTTP/1.1
 * </p>
 * For example (according to the HTTP/1.1 specification):
 * <ul>
 *  <li>A client MUST send a Host header field in all HTTP/1.1 request
 * messages.</li>
 *  <li>A user agent SHOULD send a User-Agent field in each request 
 * unless specifically configured not to do so.</li>
 * </ul>
 * It's your call to include <code>user-agent</code> or not
 * 
 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers
 * @see https://tools.ietf.org/html/rfc7230#section-5.4
 * @see https://tools.ietf.org/html/rfc7231#section-5.5.3
 */
public class HttpRequestValidator {
    private RequestValidatorConfig config;

    public HttpRequestValidator(RequestValidatorConfig config){
        this.config = config;
    }

    /**
     * It is not recommended to invalidate a request based on missing
     * common headers such as "content-length, content-type, etc...".
     * You may not need them at somepoint. In such cases, you may use
     * http {@link com.vincentcodes.webserver.annotaion.HttpHandler handlers}
     * to handle http responses individually. 
     */
    public boolean requestIsValid(HttpRequest request){
        // Just in case our parser cannot parse the first line of the request
        if(request.getBasicInfo() == null)
            return false;
        boolean isHttp1_1 = request.getBasicInfo().getVersion().equals("HTTP/1.1");
        boolean isHttp2 = request.getBasicInfo().getVersion().equals("HTTP/2") || request.getBasicInfo().getVersion().equals("HTTP/2.0");
        if(!(isHttp1_1 || isHttp2)){
            return false; // other versions are not accepted
        }
        if(isHttp2 && request.getBasicInfo().getMethod().equals("PRI")){
            return true;
        }
        if(request.getBasicInfo() == null){
            return false;
        }
        if(request.getBasicInfo().getPath().willMoveToParentDirectory()){
            return false;
        }

        HttpHeaders requestHeaders = request.getHeaders();
        for(String mandatoryHeader : config.getMandatoryHeaders()){
            if(!requestHeaders.hasHeader(mandatoryHeader)){
                return false;
            }
        }
        return true;
    }
}
