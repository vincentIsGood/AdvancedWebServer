package com.vincentcodes.tests.others;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.vincentcodes.tests.utils.RequestGenerator;
import com.vincentcodes.webserver.HttpHandlerRegister;
import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.RequestParser;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.component.response.ResponseParser;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategyFactory;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategyFactory.InvocationTypes;
import com.vincentcodes.webserver.dispatcher.operation.impl.HttpDispatcherOperation;

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing class ResponseParser")
public class ResponseParserTest {
    private WebServer server;
    private HttpRequestDispatcher dispatcher;

    private HttpRequest sampleRequest;

    @BeforeAll
    public void setup() throws IOException{
        HttpHandlerRegister.clear();
        server = new WebServer.Builder()
            .setHomeDirectory("./")
            .build();
        OperationStrategyFactory factory = new OperationStrategyFactory(server.getConfiguration());
        dispatcher = HttpRequestDispatcher.createInstance(Arrays.asList(new HttpDispatcherOperation(factory.create(InvocationTypes.NORMAL_HTTP))));
    }

    @Test
    public void test_response_is_successfully_parsed() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.GET.generateRequest("/readme.txt"));
        ResponseBuilder response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(response.asString().getBytes());
        baos.writeBytes(response.getBody().getBytes());

        ResponseBuilder parsedResponse = ResponseParser.parse(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals(response.asString(), parsedResponse.asString());
        assertEquals(Arrays.toString(response.getBody().getBytes()), Arrays.toString(parsedResponse.getBody().getBytes()));
    }

    @Test
    public void test_if_chunked_is_parsed() throws InvocationTargetException{
        String test = "HTTP/1.1 200\r\n" +
                      "Vary: Origin\r\n" +
                      "Vary: Access-Control-Request-Method\r\n" +
                      "Vary: Access-Control-Request-Headers\r\n" +
                      "Content-Type: application/json\r\n" +
                      "Transfer-Encoding: chunked\r\n" +
                      "Date: Fri, 09 Apr 2021 16:35:15 GMT\r\n" + 
                      "\r\n" + 
                      "1\r\n" + 
                      "a\r\n" + 
                      "0\r\n" + 
                      "\r\n";
        ResponseBuilder parsedResponse = ResponseParser.parse(new ByteArrayInputStream(test.getBytes()));
        System.out.println("Result: " + parsedResponse.getBody().string());
    }

    @AfterEach
    public void close() throws IOException{
        if(sampleRequest != null) sampleRequest.close();
        // if(response != null) response.close();
    }

    @AfterAll
    public void closeEverything() throws IOException{
        server.close();
    }
}
