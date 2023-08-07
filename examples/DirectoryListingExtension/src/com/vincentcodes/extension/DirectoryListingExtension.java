package com.vincentcodes.extension;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.annotaion.AutoInjected;
import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.annotaion.response.Mutatable;
import com.vincentcodes.webserver.annotaion.wrapper.InvocationCondition;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.reflect.DirectoryOnly;
import com.vincentcodes.webserver.dispatcher.reflect.NoDirectory;
import com.vincentcodes.webserver.util.DirectoryListing;
import com.vincentcodes.webserver.util.FileControl;

@HttpHandler
public class DirectoryListingExtension{
    @AutoInjected
    public WebServer server;
    
    @Mutatable
    @InvocationCondition({DirectoryOnly.class})
    @RequestMapping("/**")
    public Object catchAllDirectoryHandler(HttpRequest req, ResponseBuilder res){
        WebServer.Configuration config = server.getConfiguration();
        res.getHeaders().add("content-type", "text/html");
        return DirectoryListing.getDirectoryListingHtml(req, FileControl.get(req, config));
    }

    @Mutatable
    @InvocationCondition({NoDirectory.class})
    @RequestMapping("/**")
    public Object catchAllHandler(HttpRequest req, ResponseBuilder res){
        WebServer.Configuration config = server.getConfiguration();
        return FileControl.get(req, config);
    }
}