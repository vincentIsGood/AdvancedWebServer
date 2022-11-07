package com.vincentcodes.websocket;

import java.io.IOException;
import java.io.InputStream;

import com.vincentcodes.websocket.constants.FrameMask;
import com.vincentcodes.websocket.exception.InvalidWebSocketFrame;
import static com.vincentcodes.webserver.util.ByteUtils.*;

/**
 * frame-fin              ; 1 bit in length
 * frame-rsv1             ; 1 bit in length
 * frame-rsv2             ; 1 bit in length
 * frame-rsv3             ; 1 bit in length
 * frame-opcode           ; 4 bits in length
 * 
 * frame-masked           ; 1 bit in length
 * frame-payload-length   ; either 7, 7+16,
 *                        ; or 7+64 bits in
 *                        ; length
 * 
 * [ frame-masking-key ]  ; 32 bits in length
 * frame-payload-data     ; n*8 bits in
 *                        ; length, where
 *                        ; n >= 0
 * 
 * (most Internet protocols use UTF-8 nowadays) The "Payload data" is text data
 * encoded as UTF-8
 * 
 * @see https://tools.ietf.org/html/rfc6455#section-5.2
 * @see https://stackoverflow.com/questions/43529031/websockets-and-text-encoding
 */
/**
 * This class provides the tools to parse websocket basic frames
 */
public class WebSocketFrameParser {
    public static WebSocketFrame parse(InputStream is) throws IOException{
        WebSocketFrame frame = new WebSocketFrame();
        try{
            byte[] nextTwoBytes = new byte[2];
            byte[] nextFourBytes = new byte[4];
            byte[] nextEightBytes = new byte[8];

            // Start.
            is.read(nextTwoBytes);

            frame.fin = (byte)((toUnsignedByte(nextTwoBytes[0]) & FrameMask.FIN_MASK.value) >> 7);
            frame.rsv = (byte)(nextTwoBytes[0] & FrameMask.RSV_MASK.value);
            frame.opcode = (byte)(nextTwoBytes[0] & FrameMask.OPCODE_MASK.value);
            frame.isMasked = (byte)((toUnsignedByte(nextTwoBytes[1]) & FrameMask.IS_MASKED_MASK.value) >> 7);
            frame.payloadLength = (byte)(nextTwoBytes[1] & FrameMask.PAYLOAD_LEN_MASK.value);

            if(frame.payloadLength == 126){
                is.read(nextTwoBytes);
                frame.payloadLength = getIntFrom2Bytes(nextTwoBytes, 0);
            }else if(frame.payloadLength == 127){
                is.read(nextEightBytes);
                frame.payloadLength = getIntFrom4Bytes(nextEightBytes, 0);
            }

            is.read(nextFourBytes);
            frame.maskingKey = nextFourBytes;

            frame.payload = "";
            long bytesRead = 0;
            byte[] payload = new byte[4096];
            while(bytesRead < frame.payloadLength){
                if(bytesRead+4096 > frame.payloadLength){
                    payload = new byte[(int)(frame.payloadLength - bytesRead)];
                }
                bytesRead += is.read(payload);
                frame.payload += new String(decode(payload, 0, frame.maskingKey, payload.length));
            }
        }catch(IndexOutOfBoundsException e){
            throw new InvalidWebSocketFrame(e);
        }
        return frame;
    }

    /**
     * Decode the payload using the masking key.
     * Octet i of the transformed data ("transformed-octet-i") is the XOR of
     * octet i of the original data ("original-octet-i") with octet at index
     * i modulo 4 of the masking key ("masking-key-octet-j"):
     * 
     * @param payload the whole payload
     * @param startingIndex the starting index of the payload
     * @param maskingKey the masking key
     * @param payloadLength the length of the payload
     * 
     * @see https://tools.ietf.org/html/rfc6455#page-32
     */
    private static byte[] decode(byte[] payload, int startingIndex, byte[] maskingKey, int payloadLength){
        byte[] transformed = new byte[payloadLength];
        for(int i = 0; i < payloadLength; i++){
            transformed[i] = (byte)(payload[startingIndex + i] ^ maskingKey[i % 4]);
        }
        return transformed;
    }
}
