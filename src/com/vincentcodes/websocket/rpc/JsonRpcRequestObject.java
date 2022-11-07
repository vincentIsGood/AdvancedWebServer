package com.vincentcodes.websocket.rpc;

/**
 * @see https://www.jsonrpc.org/specification#parameter_structures
 */
public class JsonRpcRequestObject {
    public final String jsonrpc = "2.0"; // MUST BE "2.0"
    public String id;

    /**
     * Remote method name
     */
    public String method;
    
    /**
     * Can be named params or normal array
     */
    public Object params;

    public JsonRpcRequestObject(){}

    public JsonRpcRequestObject(String id, String method, Object params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }
}
