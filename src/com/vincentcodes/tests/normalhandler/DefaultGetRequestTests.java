package com.vincentcodes.tests.normalhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
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
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategyFactory;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategyFactory.InvocationTypes;
import com.vincentcodes.webserver.dispatcher.operation.impl.HttpDispatcherOperation;

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing class DefaultGetRequestTests")
public class DefaultGetRequestTests {
    private WebServer server;
    private HttpRequestDispatcher dispatcher;

    private HttpRequest sampleRequest;
    private ResponseBuilder response;

    @BeforeAll
    public void setup() throws IOException{
        HttpHandlerRegister.clear();
        server = new WebServer.Builder()
            .setHomeDirectory("./")
            .build();
        OperationStrategyFactory factory = new OperationStrategyFactory(server.getConfiguration());
        dispatcher = HttpRequestDispatcher.createInstance(Arrays.asList(new HttpDispatcherOperation(factory.create(InvocationTypes.NORMAL_HTTP))));
        server.injectDependencies();
    }

    @Test
    public void test_response_got_modified() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.GET.generateRequest("/README.md"));
        response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        System.out.println(new File("./").getAbsolutePath());
        assertEquals(200, response.getResponseCode());
        assertTrue(response.getBody().string().contains("Advanced Web Server"));
    }

    @Test
    public void test_response_got_modified_2() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.GET.standardGet());
        response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(404, response.getResponseCode());
    }

    @AfterEach
    public void close() throws IOException{
        if(sampleRequest != null) sampleRequest.close();
        if(response != null) response.close();
    }

    @AfterAll
    public void closeEverything() throws IOException{
        server.close();
    }
}
