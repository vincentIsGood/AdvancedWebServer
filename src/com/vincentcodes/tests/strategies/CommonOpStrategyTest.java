package com.vincentcodes.tests.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
public class CommonOpStrategyTest {
    private WebServer server;
    private HttpRequestDispatcher dispatcher;

    private HttpRequest sampleRequest;
    private ResponseBuilder response;

    @BeforeAll
    public void setup() throws IOException, ReflectiveOperationException{
        HttpHandlerRegister.clear();
        HttpHandlerRegister.register(TestHandler.class);
        server = new WebServer.Builder()
            .setHomeDirectory("./")
            .setUseDefaultHandlers(false)
            .build();
        OperationStrategyFactory factory = new OperationStrategyFactory(server.getConfiguration());
        dispatcher = HttpRequestDispatcher.createInstance(Arrays.asList(new HttpDispatcherOperation(factory.create(InvocationTypes.NORMAL_HTTP))));
    }

    @Test
    public void test_filterout_invalid_method_then_give_404() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.GET.generateRequest("/"));
        response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(404, response.getResponseCode());
    }

    @Test
    public void test_try_filter_valid_method_then_give_200() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.POST.generateRequest("/", ""));
        response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(200, response.getResponseCode());
        assertEquals("success", response.getBody().string());
    }

    @AfterEach
    public void close() throws IOException{
        if(sampleRequest != null) sampleRequest.close();
        if(response != null) response.close();
    }
}
