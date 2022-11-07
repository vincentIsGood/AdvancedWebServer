package com.vincentcodes.websocket.rpc;

/**
 * @see JsonRpcResponseObject
 */
public class JsonRpcErrorObject {
    public int code;
    public String message;
    /**
     * Mostly empty
     */
    public Object data = null;

    public JsonRpcErrorObject(){}

    public JsonRpcErrorObject(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
