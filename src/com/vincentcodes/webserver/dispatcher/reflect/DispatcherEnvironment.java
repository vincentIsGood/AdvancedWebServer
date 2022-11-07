package com.vincentcodes.webserver.dispatcher.reflect;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.request.HttpRequest;

/**
 * Data class
 */
public class DispatcherEnvironment {
    private WebServer.Configuration serverConfig;
    private HttpRequest request;

    public DispatcherEnvironment(WebServer.Configuration serverConfig, HttpRequest request){
        this.serverConfig = serverConfig;
        this.request = request;
    }

    public WebServer.Configuration getServerConfig(){
        return serverConfig;
    }

    public HttpRequest getRequest(){
        return request;
    }
}
