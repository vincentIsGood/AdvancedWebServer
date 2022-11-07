package com.vincentcodes.webserver.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;

/**
 * Inside the implementation of {@link com.vincentcodes.webserver.ServerThread#
 * handleHttpConnections() ServerThread.handleHttpConnections()}, upgrades are
 * <b>automatically</b> made according to these two conditions:
 * 
 * <p>
 * Request must has an  "Upgrade: websocket" header.
 * Response must has an "Upgrade: websocket" header.
 * 
 * <p>
 * It's possible to have this
 * <pre>
 * &#64;RequestMapping("/ws")
 * public void handleUpgradeRequest(HttpRequest request, ResponseBuilder response){
 *     WebSocketUpgrader.upgrade(request, response);
 * }
 * </pre>
 */
public class WebSocketUpgrader {
    private static MessageDigest SHA1;
    static {
        try{
            SHA1 = MessageDigest.getInstance("SHA-1");
        }catch(NoSuchAlgorithmException ignored){}
    }

    /**
     * Mutates the request and response to accommodate the requirements
     * for the upgrade (defined by the webserver)
     * @param request [mutate]
     * @param response [mutate]
     */
    public static void upgrade(HttpRequest request, ResponseBuilder response){
        if(request.getHeaders().hasHeader("upgrade")){
            String clientWSKey = request.getHeaders().getHeader("sec-websocket-key");
            
            response.setResponseCode(101);
            
            HttpHeaders headers = response.getHeaders();
            headers.add("Connection", "Upgrade");
            headers.add("Upgrade", "websocket");

            byte[] specialString = SHA1.digest((clientWSKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.UTF_8));
            headers.add("Sec-WebSocket-Accept", Base64.getEncoder().encodeToString(specialString));
        }else{
            response.setResponseCode(404);
        }
    }
}
