package com.vincentcodes.webserver.util;

import com.vincentcodes.webserver.component.response.ResponseBuilder;

public class TunnelUtils {
    /**
     * @param remoteDest remote server (eg. api.com, api.com:1234, 127.0.0.1:1234)
     * @param requestPolicy either "exchange" or "one-way"
     */
    public static void setupTunnel(ResponseBuilder res, String remoteDest, String requestPolicy){
        res.getHeaders().add("X-Vws-Raw-Tunnel", remoteDest);
        res.getHeaders().add("X-Vws-Req-Policy", requestPolicy);
    }
    /**
     * @param dangerous whether ssl checks cert or not
     * @see #setupTunnel(ResponseBuilder, String, String)
     */
    public static void setupTunnel(ResponseBuilder res, String remoteDest, String requestPolicy, boolean ssl, boolean dangerous){
        res.getHeaders().add("X-Vws-Raw-Tunnel", remoteDest);
        res.getHeaders().add("X-Vws-Req-Policy", requestPolicy);
        res.getHeaders().add("X-Vws-Ssl-Tunnel", Boolean.toString(ssl));
        res.getHeaders().add("X-Vws-Ssl-Tunnel-Danger", Boolean.toString(dangerous));
    }
}
