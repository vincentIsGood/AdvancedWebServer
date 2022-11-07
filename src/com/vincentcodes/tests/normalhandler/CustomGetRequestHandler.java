package com.vincentcodes.tests.normalhandler;

import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.request.HttpGet;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.annotaion.response.Mutatable;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;

@HttpHandler
public class CustomGetRequestHandler {

    @HttpGet
    @Mutatable
    public void handleRootGet(HttpRequest req, ResponseBuilder res){
        res.setResponseCode(200);
    }

    @HttpGet
    @Mutatable
    @RequestMapping("/asd2")
    public String handleGet2(HttpRequest req, ResponseBuilder res){
        res.setResponseCode(200);
        return "response";
    }
}
