package com.vincentcodes.tests.strategies;

import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.dispatcher.IRequestsFilter;

public class FilterOutSample implements IRequestsFilter {

    @Override
    public boolean willFilterOut(HttpRequest arg) {
        System.out.println("inspecting request.");
        return arg.getBasicInfo().getMethod().equals("GET");
    }
    
}
