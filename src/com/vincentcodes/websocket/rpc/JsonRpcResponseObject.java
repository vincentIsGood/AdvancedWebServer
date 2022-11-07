package com.vincentcodes.websocket.rpc;

public class JsonRpcResponseObject {
    public final String jsonrpc = "2.0"; // optional. But it MUST BE "2.0"
    public String id;
    public Object result;
    public JsonRpcErrorObject error;

    public JsonRpcResponseObject(){}
    
    public JsonRpcResponseObject(String id, Object result, JsonRpcErrorObject error) {
        this.id = id;
        this.result = result;
        this.error = error;
    }
}
