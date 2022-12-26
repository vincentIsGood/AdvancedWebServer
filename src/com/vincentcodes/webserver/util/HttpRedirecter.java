package com.vincentcodes.webserver.util;

import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;

/**
 * Used for basic redirections. It can be used as shown below.
 * <pre>
 * &#64;HttpGet
 * &#64;RequestMapping("/")
 * public void handleRedirect(HttpRequest req, ResponseBuilder res){
 *     HttpRedirecter.temporaryRedirect(req, res, "https://127.0.0.1:5050/redirected");
 * }
 * </pre>
 */
public class HttpRedirecter {
    /**
     * @param request
     * @param response [mutate]
     * @param url the destination of the redirection
     */
    public static void foundRedirect(HttpRequest request, ResponseBuilder response, String url){
        response.setResponseCode(302);

        HttpHeaders headers = response.getHeaders();
        headers.add("location", url);
        headers.add("cache-control", "no-store, no-cache");
    }
    
    /**
     * @param request
     * @param response [mutate]
     * @param url the destination of the redirection
     */
    public static void temporaryRedirect(HttpRequest request, ResponseBuilder response, String url){
        response.setResponseCode(307);

        HttpHeaders headers = response.getHeaders();
        headers.add("location", url);
        headers.add("cache-control", "no-store, no-cache");
    }

    /**
     * @param request
     * @param response [mutate]
     * @param url the destination of the redirection
     */
    public static void permanentRedirect(HttpRequest request, ResponseBuilder response, String url){
        response.setResponseCode(308);

        HttpHeaders headers = response.getHeaders();
        headers.add("location", url);
    }
}
