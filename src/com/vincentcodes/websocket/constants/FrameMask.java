package com.vincentcodes.websocket.constants;

public enum FrameMask{
    FIN_MASK         ((byte)0b10000000),
    RSV_MASK         ((byte)0b01110000),
    OPCODE_MASK      ((byte)0b00001111),

    IS_MASKED_MASK   ((byte)0b10000000),
    PAYLOAD_LEN_MASK ((byte)0b01111111);

    public final byte value;

    FrameMask(byte value){
        this.value = value;
    }

    /**
     * @param b
     * @return null if not found
     */
    public static FrameMask fromByte(byte b){
        for(FrameMask ele : values()) if(ele.value == b) return ele;
        return null;
    }
}