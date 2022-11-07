package com.vincentcodes.tests.normalhandler;

import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.request.HttpPost;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.component.request.HttpRequest;

@HttpHandler
public class TestPostRequestHandler {
    @HttpPost
    @RequestMapping("/test")
    public String handlePostRequestTest(HttpRequest req){
        return req.getBody().string();
    }

    @HttpPost
    @RequestMapping("/test_form")
    public String handlePostRequestTestForm(HttpRequest req){
        if(req.hasMultipartData()){
            return req.getMultipartFormData().getData("testfile").getBody().string();
        }
        return "null";
    }
}
