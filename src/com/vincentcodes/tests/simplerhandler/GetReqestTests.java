package com.vincentcodes.tests.simplerhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.vincentcodes.tests.utils.RequestGenerator;
import com.vincentcodes.webserver.ExtensionRegister;
import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.RequestParser;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategyFactory;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategyFactory.InvocationTypes;
import com.vincentcodes.webserver.dispatcher.operation.impl.SimplerHttpDispatcherOperation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing class SimplerHttpHandlerInvocation")
public class GetReqestTests {
    private WebServer server;
    private HttpRequestDispatcher dispatcher;

    @BeforeAll
    public void setup() throws IOException{
        ExtensionRegister.clear();
        ExtensionRegister.register(TestSimpleHttpHandler.class);
        server = new WebServer.Builder()
            .setHomeDirectory("C:/Users/vincent/Documents/zPrograms/Java/0_OwnProjects/0_SmallPrograms/AdvancedWebServer")
            .build();
        OperationStrategyFactory factory = new OperationStrategyFactory(server.getConfiguration());
        dispatcher = HttpRequestDispatcher.createInstance(Arrays.asList(new SimplerHttpDispatcherOperation(factory.create(InvocationTypes.SIMPLER_HTTP))));
    }

    @Test
    public void test_if_response_got_modified() throws InvocationTargetException{
        HttpRequest request = RequestParser.parse(RequestGenerator.GET.generateRequest("/readme.txt?get=123.5&json={\"name\":\"vincent\", \"age\":19, \"desc\":\"I love anime\"}"));
        ResponseBuilder response = dispatcher.dispatchObjectToHandlers(request);
        assertEquals(200, response.getResponseCode());
        assertEquals("null 123.500000 {name: vincent, age: 19, desc: I love anime}", response.getBody().string());
    }

    @Test
    public void test_if_response_returns_json() throws InvocationTargetException{
        HttpRequest request = RequestParser.parse(RequestGenerator.GET.generateRequest("/others"));
        ResponseBuilder response = dispatcher.dispatchObjectToHandlers(request);
        assertEquals(200, response.getResponseCode());
        assertEquals("{\"name\":\"vincent\",\"age\":19,\"desc\":\"I love anime\"}", response.getBody().string());
    }

    @Test
    public void test_if_response_returns_json_array() throws InvocationTargetException{
        HttpRequest request = RequestParser.parse(RequestGenerator.GET.generateRequest("/others_array"));
        ResponseBuilder response = dispatcher.dispatchObjectToHandlers(request);
        assertEquals(200, response.getResponseCode());
        assertEquals("[{\"name\":\"vincent\",\"age\":19,\"desc\":\"I love anime\"}]", response.getBody().string());
    }

    @Test
    public void test_if_response_returns_null_successfully() throws InvocationTargetException{
        HttpRequest request = RequestParser.parse(RequestGenerator.GET.generateRequest("/null"));
        ResponseBuilder response = dispatcher.dispatchObjectToHandlers(request);
        assertEquals(200, response.getResponseCode());
    }

    @Test
    public void test_if_server_handles_unknown_objects_successfully() throws InvocationTargetException{
        HttpRequest request = RequestParser.parse(RequestGenerator.GET.generateRequest("/unknown_object"));
        ResponseBuilder response = dispatcher.dispatchObjectToHandlers(request);
        assertEquals(500, response.getResponseCode());
    }

    @AfterAll
    public void closeEverything() throws IOException{
        server.close();
    }
}
