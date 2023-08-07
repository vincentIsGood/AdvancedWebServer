package com.vincentcodes.webserver.util;

import com.vincentcodes.webserver.component.response.ResponseBuilder;

public class TunnelUtils {
    /**
     * @param remoteDest remote server (eg. api.com, api.com:1234, 127.0.0.1:1234)
     * @param exchangeRequest whether the request will be sent to remote server
     */
    public static void setupTunnel(ResponseBuilder res, String remoteDest, boolean exchangeRequest){
        res.getHeaders().add("X-Vws-Raw-Tunnel", remoteDest);
        res.getHeaders().add("X-Vws-Exchange-Once", Boolean.toString(exchangeRequest));
    }
}
