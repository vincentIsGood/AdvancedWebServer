package com.vincentcodes.tests.normalhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing class HttpHandlerInvocation")
public class DefaultGetRequestTests {
    private WebServer server;
    private HttpRequestDispatcher dispatcher;

    @BeforeAll
    public void setup() throws IOException{
        HttpHandlerRegister.clear();
        server = new WebServer.Builder()
            .setHomeDirectory("D:\\Downloads_D\\zPrograms\\Java\\0_OwnProjects\\0_SmallPrograms\\AdvancedWebServer")
            .build();
        OperationStrategyFactory factory = new OperationStrategyFactory(server.getConfiguration());
        dispatcher = HttpRequestDispatcher.createInstance(Arrays.asList(new HttpDispatcherOperation(factory.create(InvocationTypes.NORMAL_HTTP))));
        server.injectDependencies();
    }

    @Test
    public void test_response_got_modified() throws InvocationTargetException{
        HttpRequest sampleRequest = RequestParser.parse(RequestGenerator.GET.generateRequest("/readme.txt"));
        ResponseBuilder response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(200, response.getResponseCode());
        assertTrue(response.getBody().string().contains("Project Name: Vincent Web Server"));
    }

    @Test
    public void test_response_got_modified_2() throws InvocationTargetException{
        HttpRequest sampleRequest = RequestParser.parse(RequestGenerator.GET.standardGet());
        ResponseBuilder response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(404, response.getResponseCode());
    }

    @AfterAll
    public void closeEverything() throws IOException{
        server.close();
    }
}
