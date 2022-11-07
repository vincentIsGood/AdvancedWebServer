package com.vincentcodes.webserver.defaults;

import java.io.IOException;

import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.request.HttpPri;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;

/**
 * Inside the implementation of {@link com.vincentcodes.webserver.ServerThread#
 * handleHttpConnections() ServerThread.handleHttpConnections()}, upgrades are
 * <b>automatically</b> made according to these two conditions:
 * <p>
 * Request must send a connection preface with a "PRI" method.
 * Response must be empty.
 * <p>
 * Note: Asterisk (*) refers to the entire server.
 * 
 * @see https://tools.ietf.org/html/rfc7540
 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/OPTIONS
 */
@HttpHandler
public class Http2UpgradeHandler {
    @HttpPri
    @RequestMapping(value = "*", exact = true)
    public void handleUpgradeRequest(HttpRequest request, ResponseBuilder response) throws IOException {
        if(request.getBasicInfo().getMethod().equals("PRI")){
            // h2c is not used most of the time...
            // https://stackoverflow.com/questions/46788904/why-do-web-browsers-not-support-h2c-http-2-without-tls
            //
            // So, I will not implement it.
            response.empty();
        }else{
            // Response code 426 is ignored since the protocol is chosen
            // at TLS handsake from one of the the enabled ALPN values.
            response.setResponseCode(404);
        }
    }
}
