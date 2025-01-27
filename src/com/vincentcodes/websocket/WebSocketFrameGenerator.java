package com.vincentcodes.websocket;

import com.vincentcodes.websocket.constants.FrameConstants;
import com.vincentcodes.websocket.constants.OpCode;

/**
 * This class is used to generate various websocket frames.
 * For opcode, please make reference to {@link FrameConstants.OpCode}.
 */
public class WebSocketFrameGenerator {
    public WebSocketFrameGenerator(){}

    /**
     * more frames of this message follow
     */
    public WebSocketFrame basicContinueFrame(OpCode opcode, String content){
        return basicFrame((byte)0, opcode, content);
    }

    /**
     * Indicates that this is the final fragment in a message. The first
     * fragment MAY also be the final fragment.
     */
    public WebSocketFrame basicCloseFrame(OpCode opcode, String content){
        return basicFrame((byte)1, opcode, content);
    }

    public WebSocketFrame basicFrame(byte fin, OpCode opcode, String content){
        return frame(fin, opcode, content, FrameConstants.StatusCodes.NONE);
    }

    public WebSocketFrame closeConnectionFrame(String reason, short statusCode){
        return frame((byte)1, OpCode.CLOSE, reason, statusCode);
    }

    public WebSocketFrame pingFrame(){
        return frame((byte)1, OpCode.PING, "", FrameConstants.StatusCodes.NONE);
    }
    public WebSocketFrame pongFrame(){
        return frame((byte)1, OpCode.PONG, "", FrameConstants.StatusCodes.NONE);
    }

    /**
     * @see WebSocketFrameGenerator#frame(byte, byte, String, short)
     */
    public WebSocketFrame frame(byte fin, OpCode opcode, String content, short statusCode){
        return frame(fin, opcode.value, content, statusCode);
    }

    /**
     * Basic websocket frame
     * @param fin 0 or 1
     * @param opcode {@link FrameConstants.OpCode}
     * @param content aka payload
     * @param statusCode used when server / client closes the connection {@link FrameConstants.StatusCodes}
     * @see https://tools.ietf.org/html/rfc6455#section-5.5.1
     */
    public WebSocketFrame frame(byte fin, byte opcode, String content, short statusCode){
        if(content == null) content = "";
        WebSocketFrame frame = new WebSocketFrame();
        frame.fin = fin;
        frame.rsv = 0;
        frame.opcode = opcode;
        frame.isMasked = 0;
        frame.payloadLength = content.length() + (statusCode <= 0? 0 : 2);
        frame.maskingKey = null;
        frame.payload = content;
        frame.statusCode = (short)statusCode;
        return frame;
    }
}
