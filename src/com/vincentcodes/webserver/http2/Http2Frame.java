package com.vincentcodes.webserver.http2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vincentcodes.webserver.util.ByteUtils;

/**
 * Frame Format (in sequence):
 * Length            - 24 bits (3 bytes)  - length of the frame payload
 * Type              - 8 bits  (1 byte)   - type of the frame (we have values of 0-9 as of 2021)
 * Flags             - 8 bits  (1 byte)   - flags
 * R (reserved)      - 1 bit              - undefined
 * Stream Identifier - 31 bits (4 bytes)  - https://tools.ietf.org/html/rfc7540#section-5.1.1
 * Frame Payload     - any bits           - payload (frame payload is dependent entirely on the frame type)
 * 
 * @see https://tools.ietf.org/html/rfc7540#section-4.1
 */
public class Http2Frame {
    public int payloadLength; // 3 bytes are used
    public byte type;
    public byte flags;
    public int streamIdentifier; // leftmost bit is the reserved bit (R) it will be 0x0
    public Http2FrameType payload;

    public static byte[] toBytes(Http2Frame frame){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            byte[] length = ByteUtils.intToByteArray(frame.payloadLength);
            stream.write(new byte[]{length[1], length[2], length[3]});
            stream.write(frame.type);
            stream.write(frame.flags);
            stream.write(ByteUtils.intToByteArray(frame.streamIdentifier));
            if(frame.payload != null)
                stream.write(frame.payload.toBytes());
        }catch(IOException e){}
        return stream.toByteArray();
    }

    public static void streamBytesTo(Http2Frame frame, OutputStream stream) throws IOException{
        byte[] length = ByteUtils.intToByteArray(frame.payloadLength);
        stream.write(new byte[]{length[1], length[2], length[3]});
        stream.write(frame.type);
        stream.write(frame.flags);
        stream.write(ByteUtils.intToByteArray(frame.streamIdentifier));
        if(frame.payload != null)
            frame.payload.streamBytesTo(stream);
    }

    // This method hinders performance (you may comment out all toString methods)
    public static String getString(Http2Frame frame){
        return String.format("{Http2Frame length: %d, type: %d, flags: %d, stream: %d, payload: %s}", frame.payloadLength, frame.type, frame.flags, frame.streamIdentifier, frame.payload);
    }
    
}
