package com.vincentcodes.tests.others;

import com.vincentcodes.tests.utils.RequestGenerator;
import com.vincentcodes.webserver.component.request.RequestParser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Testing class RequestParser")
public class RequestParserTest {
    @Test
    public void test_simple_request() throws Exception{
        RequestParser.parse(RequestGenerator.GET.generateRequest("/asd"));
    }
    
    @Test
    public void test_complex_request() throws Exception{
        RequestParser.parse(RequestGenerator.GET.standardGet());
    }
}
