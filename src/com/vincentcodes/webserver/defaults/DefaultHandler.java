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

    // @Mutatable
    // @InvocationCondition({DirectoryOnly.class})
    // @RequestMapping("/**")
    // public Object catchAllDirectoryHandler(HttpRequest req, ResponseBuilder res){
    //     WebServer.Configuration config = server.getConfiguration();
    //     res.getHeaders().add("content-type", "text/html");
    //     return DirectoryListing.getDirectoryListingHtml(req, FileControl.get(req, config));
    // }

    // @Mutatable
    // @HttpPut
    // @RequestMapping("/samplewebpages/*")
    // public void handlePutRequests(HttpRequest req, ResponseBuilder res){
    //     res.setResponseCode(201);
    //     FileControl.save(req, server.getConfiguration());
    // }

    // @RequestMapping("/**")
    // public void tunnelWebapp(HttpRequest req, ResponseBuilder res) throws IOException, InterruptedException{
    //     TunnelUtils.setupTunnel(res, "127.0.0.1:5023", true);
    // }

    // @RequestMapping("/**")
    // public Object serveStatic(HttpRequest req, ResponseBuilder res){
    //     File file = FileControl.get(req, server.getConfiguration());
    //     if(req.getBasicInfo().getParameter("download") != null){
    //         return new HttpHandlerResult(file, new HttpHandlerOptions.Builder().asAttachment().build());
    //     }
    //     return file;
    // }
    
    // ------- Default ------- //
    @Mutatable
    @RequestMapping("/**")
    public Object catchAllHandler(HttpRequest req, ResponseBuilder res){
        WebServer.Configuration config = server.getConfiguration();
        return FileControl.get(req, config);
    }
}
