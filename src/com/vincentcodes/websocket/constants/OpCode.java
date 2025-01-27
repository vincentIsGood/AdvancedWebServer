package com.vincentcodes.websocket.constants;

/**
 * opcodes are defined here
 * @see https://tools.ietf.org/html/rfc6455#section-5.2
 */
public enum OpCode {
    // Use the opcode based on the previous frame
    CONTINUE ((byte) 0),

    TEXT     ((byte) 1),
    BINARY   ((byte) 2),

    // Closes connection
    CLOSE    ((byte) 8),

    PING     ((byte) 9),
    PONG     ((byte) 10);

    // 3 to 7 are not supported
    // static byte NON_CONTROL = 3;

    // 0xB to 0xF are not supported
    // static byte OTHERS = 0xB;

    public final byte value;

    OpCode(byte value){
        this.value = value;
    }

    /**
     * @param b
     * @return null if not found
     */
    public static OpCode fromByte(byte b){
        for(OpCode ele : values()) if(ele.value == b) return ele;
        return null;
    }
}
