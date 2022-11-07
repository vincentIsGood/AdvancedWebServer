package com.vincentcodes.websocket.handler;

import com.vincentcodes.websocket.rpc.JsonRpcRequestObject;
import com.vincentcodes.websocket.rpc.JsonRpcResponseObject;

public interface OnJsonRpcReceive {
    public JsonRpcResponseObject handle(JsonRpcRequestObject payload);
}
