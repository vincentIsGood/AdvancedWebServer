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
import com.vincentcodes.webserver.ExtensionRegister;
import com.vincentcodes.webserver.WebServer;
import com.vincentcodes.webserver.component.request.HttpRequest;
import com.vincentcodes.webserver.component.request.RequestParser;
import com.vincentcodes.webserver.component.response.ResponseBuilder;
import com.vincentcodes.webserver.dispatcher.HttpRequestDispatcher;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategyFactory;
import com.vincentcodes.webserver.dispatcher.operation.OperationStrategyFactory.InvocationTypes;
import com.vincentcodes.webserver.dispatcher.operation.impl.HttpDispatcherOperation;

@TestInstance(Lifecycle.PER_CLASS)
@DisplayName("Testing Post Requests")
public class PostRequestTests {
    private WebServer server;
    private HttpRequestDispatcher dispatcher;

    private HttpRequest sampleRequest;
    private ResponseBuilder response;

    @BeforeAll
    public void setup() throws IOException{
        ExtensionRegister.clear();
        ExtensionRegister.register(TestPostRequestHandler.class);
        server = new WebServer.Builder()
            .setHomeDirectory("./")
            .build();
        OperationStrategyFactory factory = new OperationStrategyFactory(server.getConfiguration());
        dispatcher = HttpRequestDispatcher.createInstance(Arrays.asList(new HttpDispatcherOperation(factory.create(InvocationTypes.NORMAL_HTTP))));
    }

    @Test
    public void is_content_put_into_httpbody() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.POST.generateRequest("/test", "key=asd&key2=asd"));
        response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(200, response.getResponseCode());
        assertEquals("key=asd&key2=asd", response.getBody().string());
    }

    @Test
    public void is_file_using_multipart_correctly_posted() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.POST.generateMultipartRequest("/test_form"));
        assertEquals(true, sampleRequest.hasMultipartData());
        assertEquals("厳しい.txt", sampleRequest.getMultipartFormData().getData("testfile").getFilename());
    }

    @Test 
    public void is_file_using_multipart_correctly_responded() throws InvocationTargetException{
        sampleRequest = RequestParser.parse(RequestGenerator.POST.generateMultipartRequest("/test_form"));
        response = dispatcher.dispatchObjectToHandlers(sampleRequest);
        assertEquals(200, response.getResponseCode());
        assertEquals("This is a test file.\nThis is a test file.", response.getBody().string());
    }

    @AfterEach
    public void close() throws IOException{
        if(sampleRequest != null) sampleRequest.close();
        if(response != null) response.close();
    }

    @AfterAll
    public void closeAll() throws IOException{
        server.close();
    }
}
