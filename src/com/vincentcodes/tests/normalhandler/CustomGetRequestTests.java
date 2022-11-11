package com.vincentcodes.tests.normalhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
@DisplayName("Testing class HttpHandlerInvocation")
public class CustomGetRequestTests {
    private WebServer server;
    private HttpRequestDispatcher dispatcher;

    private HttpRequest sampleRequest;
    private ResponseBuilder response;

    @BeforeAll
    public void setup() throws IOException, ReflectiveOperationException{
        HttpHandlerRegister.clear();
        HttpHandlerRegister.register(CustomGetRequestHandler.class);
        server = new WebServer.Builder()
            .setHomeDirectory("D:\\Downloads_D\\zPrograms\\Java\\0_OwnProjects\\0_SmallPrograms\\AdvancedWebServer")
            .build();
        OperationStrategyFactory factory = new OperationStrategyFactory(server.getConfiguration());
        dispatcher = HttpRequestDispatcher.createInstance(Arrays.asList(new HttpDispatcherOperation(factory.create(InvocationTypes.NORMAL_HTTP))));
        server.injectDependencies();
    }
    
    @Test
    public void requestmapping_less_response_positive() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.GET.generateRequest("/"));
        response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(200, response.getResponseCode());
    }
    
    @Test
    public void requestmapping_less_response_negative() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.GET.generateRequest("/asd"));
        response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(404, response.getResponseCode());
    }
    
    @Test
    public void requestmapping_less_response_negative2() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.POST.generateRequest("/", ""));
        response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(404, response.getResponseCode());
    }

    @Test
    public void requestmapping_response_positive() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.GET.generateRequest("/asd2"));
        response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(200, response.getResponseCode());
        assertEquals("response", response.getBody().string());
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
