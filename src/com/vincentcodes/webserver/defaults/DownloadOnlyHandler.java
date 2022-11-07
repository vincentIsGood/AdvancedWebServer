package com.vincentcodes.webserver.defaults;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.annotaion.AutoInjected;
import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.request.HttpGet;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.component.body.HttpBody;
import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;

@HttpHandler
public class DownloadOnlyHandler {
    @AutoInjected
    public WebServer server;

    @RequestMapping("/favicon.ico")
    public void handleFavIcon(HttpRequest req, ResponseBuilder res){
        res.setResponseCode(404);
    }

    @HttpGet
    @RequestMapping("/**")
    public void catchAllHandler(HttpRequest req, ResponseBuilder res){
        WebServer.Configuration config = server.getConfiguration();
        File file = new File(config.getHomeDirectory() + req.getBasicInfo().getPath());
        
        HttpHeaders headers = res.getHeaders();
        headers.add("cache-control", "no-store");
        headers.add("content-disposition", "attachment; filename=" + file.getName());
        headers.add("content-length", Long.toString(file.length()));
        headers.add("content-type", "application/octet-stream");

        HttpBody body = res.getBody();
        try(FileInputStream is = new FileInputStream(file)){
            byte[] buffer = new byte[(int)file.length()];
            is.read(buffer);
            body.writeToBody(buffer);
        }catch(IOException e){
            throw new RuntimeException("Cannot read file: " + file.getAbsolutePath(), e);
        }
    }
}
