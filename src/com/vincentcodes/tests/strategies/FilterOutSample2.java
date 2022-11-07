package com.vincentcodes.tests.strategies;

import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.dispatcher.IRequestsFilter;

public class FilterOutSample2 implements IRequestsFilter {

    @Override
    public boolean willFilterOut(HttpRequest arg) {
        return arg.getBasicInfo().getMethod().equals("HEAD");
    }
    
}
