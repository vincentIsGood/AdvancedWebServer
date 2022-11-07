package com.vincentcodes.tests.others;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.vincentcodes.tests.utils.RequestGenerator;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.RequestParser;
import com.vincentcodes.webserver.helper.HttpTunnel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing class HttpTunnel")
public class HttpTunnelTest {
    
    @Test
    public void sentRequest_is_valid() throws IOException{
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        HttpRequest request = RequestParser.parse(RequestGenerator.POST.generateRequest("Test", "help"));
        HttpTunnel.writeRequestToOutputStream(output, request);
        assertEquals(RequestGenerator.POST.generateRequest("Test", "help"), output.toString());
    }
}
