package com.vincentcodes.tests.strategies;

import com.vincentcodes.webserver.annotaion.HttpHandler;
import com.vincentcodes.webserver.annotaion.request.RequestMapping;
import com.vincentcodes.webserver.annotaion.request.RequestsFilter;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.response.ResponseBuilder;

@HttpHandler
@RequestsFilter({FilterOutSample.class, FilterOutSample2.class})
public class TestHandler {
    @RequestMapping("/")
    public String test(HttpRequest request, ResponseBuilder response){
        return "success";
    }
}
