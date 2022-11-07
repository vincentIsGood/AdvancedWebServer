package com.vincentcodes.tests.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.vincentcodes.json.CannotMapFromObjectException;
import com.vincentcodes.json.ObjectMapper;
import com.vincentcodes.json.ObjectMapperConfig;
import com.vincentcodes.websocket.rpc.JsonRpcErrorObject;
import com.vincentcodes.websocket.rpc.JsonRpcRequestObject;
import com.vincentcodes.websocket.rpc.JsonRpcResponseObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class RpcObjectMapperTest {
    private ObjectMapper mapper;

    @BeforeAll
    public void setup(){
        mapper = new ObjectMapper(
            new ObjectMapperConfig.Builder()
            .setAllowMissingProperty(true)
            .setDebugModeOn(true)
            .build());
    }

    @Test
    public void jsonrpcreq_stringify() throws CannotMapFromObjectException{
        JsonRpcRequestObject rpc = new JsonRpcRequestObject();
        rpc.id = "1.0";
        rpc.method = "Test.run";
        rpc.params = new int[]{1,2,3,4};
        assertEquals("{\"jsonrpc\":\"2.0\",\"id\":\"1.0\",\"method\":\"Test.run\",\"params\":[1,2,3,4]}", mapper.objectToJson(rpc));
    }

    @Test
    public void jsonrpcres_stringify() throws CannotMapFromObjectException{
        JsonRpcResponseObject rpc = new JsonRpcResponseObject();
        rpc.id = "1.0";
        rpc.result = "Test.run";
        rpc.error = new JsonRpcErrorObject(400, "err", "send help");
        assertEquals("{\"jsonrpc\":\"2.0\",\"id\":\"1.0\",\"result\":\"Test.run\",\"error\":{\"code\":400,\"message\":\"err\",\"data\":\"send help\"}}", mapper.objectToJson(rpc));
    }
}
