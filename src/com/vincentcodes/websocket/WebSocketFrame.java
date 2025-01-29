package com.vincentcodes.websocket;

import static com.vincentcodes.webserver.util.ByteUtils.longToByteArray;
import static com.vincentcodes.webserver.util.ByteUtils.shortToByteArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.vincentcodes.websocket.constants.OpCode;

public class WebSocketFrame {
    byte fin; // 1 bit (fin = 1/0)
    byte rsv; // 3 bits (reserved bits)
    byte opcode; // 4 bits
    byte isMasked; // 1 bit
    long payloadLength; // in utf-8 bytes
    byte[] maskingKey;
    String payload; // max length() == Integer.MAX_VALUE characters
    short statusCode = -1;

    byte[] payloadInBytes; // be sure to set this for toBytes

    public byte getFin() {
        return fin;
    }

    public byte getRsv() {
        return rsv;
    }

    public byte getOpcodeByte() {
        return opcode;
    }
    public OpCode getOpcode() {
        return OpCode.fromByte(opcode);
    }

    public byte getIsMasked() {
        return isMasked;
    }

    public long getPayloadLength() {
        return payloadLength;
    }

    public byte[] getMaskingKey() {
        return maskingKey;
    }

    public String getPayload() {
        return payload;
    }

    /**
     * Turn the frame object into bytes which can be sent 
     * to the client without touching it.
     */
    public byte[] toBytes() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            os.write(fin << 7 | rsv << 4 | opcode);

            if(payloadLength > Integer.MAX_VALUE){
                os.write(isMasked << 7 | 127);
                os.write(longToByteArray(payloadLength));
            }else if(payloadLength > 125){
                os.write(isMasked << 7 | 126);
                os.write(shortToByteArray((short)payloadLength));
            }else{
                os.write(isMasked << 7 | (byte)payloadLength);
            }

            if(maskingKey != null){
                os.write(maskingKey);
            }
            
            if(statusCode != -1){
                os.write(shortToByteArray(statusCode));
            }

            os.write(payloadInBytes);
        }catch(IOException e){}
        return os.toByteArray();
    }
    
    public String toString(){
        return String.format("{WsFrame fin: %d, opcode: %d, statusCode: %d, payload: %s}", fin, opcode, statusCode, payload);
    }
}