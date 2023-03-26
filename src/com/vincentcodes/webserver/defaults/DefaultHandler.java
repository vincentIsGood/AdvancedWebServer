package com.vincentcodes.webserver.defaults;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.annotaion.AutoInjected;
import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.annotaion.response.Mutatable;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.util.FileControl;

/**
 * This class is used to serve any kind of files normally (enabled by default)
 * @see WebServer
 */
@HttpHandler
public class DefaultHandler {
    @AutoInjected
    public WebServer server;

    /**
     * This demonstrates the flexibility to mutate a response to suit your needs.
     * Though if you want these things to be smartly automated, you may want to
     * check out &#64;SimpleHttpHandler
     */
    // @RequestMapping(value = "/test/returnjson")
    // public String handleFavicon(HttpRequest req, ResponseBuilder response){
    //     response.setResponseCode(200);
    //     response.getHeaders().add("connection", "close");
    //     response.getHeaders().add("content-type", "text/json");
    //     return "{'msg':'nothing special here'}";
    // }

    /**
     * This method is flawed, since it handles the favicon at the root
     * of the directory only.
     */
    // @RequestMapping("/favicon.ico")
    // public void handleFavIcon(HttpRequest req, ResponseBuilder res){
    //     res.setResponseCode(404);
    // }

    // @Mutatable
    // @InvocationCondition({DirectoryOnly.class})
    // @RequestMapping("/**")
    // public Object catchAllDirectoryHandler(HttpRequest req, ResponseBuilder res){
    //     WebServer.Configuration config = server.getConfiguration();
    //     res.getHeaders().add("content-type", "text/html");
    //     return DirectoryListing.getDirectoryListingHtml(req, FileControl.get(req, config));
    // }

    // @Mutatable
    // @InvocationCondition({NoDirectory.class})
    // @RequestMapping("/**")
    // public Object catchAllFileHandler(HttpRequest req, ResponseBuilder res){
    //     WebServer.Configuration config = server.getConfiguration();
    //     return FileControl.get(req, config);
    // }

    // @RequestMapping("/ws")
    // public void wsUpgrade(HttpRequest req, ResponseBuilder res){
    //     WebSocketUpgrader.upgrade(req, res);
    // }

    // @Mutatable
    // @HttpPut
    // @RequestMapping("/samplewebpages/*")
    // public void handlePutRequests(HttpRequest req, ResponseBuilder res){
    //     res.setResponseCode(201);
    //     res.getHeaders().add("content-type", "text/plain");
    //     res.getHeaders().add("content-length", "0");
    //     FileControl.save(req, server.getConfiguration());
    // }

    // ------- TODO: I still cannot download a large file ------- //
    // @Mutatable
    // @RequestMapping("/**")
    // public Object catchAllHandler(HttpRequest req, ResponseBuilder res){
    //     WebServer.Configuration config = server.getConfiguration();
    //     // only works for 2MiB
    //     return new HttpHandlerResult(FileControl.get(req, config), new HttpHandlerOptions.Builder().wholeFile().build());
    // }
    
    // ------- Default ------- //
    @Mutatable
    @RequestMapping("/**")
    public Object catchAllHandler(HttpRequest req, ResponseBuilder res){
        WebServer.Configuration config = server.getConfiguration();
        return FileControl.get(req, config);
    }
}
