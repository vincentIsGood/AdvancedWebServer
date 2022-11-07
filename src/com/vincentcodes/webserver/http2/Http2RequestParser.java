package com.vincentcodes.webserver.http2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.vincentcodes.webserver.exception.CannotParseRequestException;
import com.vincentcodes.webserver.exception.InvalidFrameTypeException;
import com.vincentcodes.webserver.http2.constants.FrameTypes;
import com.vincentcodes.webserver.http2.hpack.HpackDecoder;
import com.vincentcodes.webserver.util.ByteUtils;

public class Http2RequestParser {
    private Http2Configuration config;
    private HpackDecoder hpackDecoder;

    public Http2RequestParser(HpackDecoder hpackDecoder, Http2Configuration config){
        this.config = config;
        this.hpackDecoder = hpackDecoder;
    }

    /**
     * <p>
     * This is a blocking function since InputStream.read is used.
     * <p>
     * 0x1 stream is used immediately after the upgrade.
     * <p>
     * To parse frame type specific frames, refer to {@link FrameTypes#parseFunc} 
     * attribute for each type.
     * 
     * @see https://tools.ietf.org/html/rfc7540#section-4.1
     */
    public Http2Frame parse(InputStream is) throws IOException, CannotParseRequestException{
        Http2Frame frame = new Http2Frame();
        // ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        try{
            byte[] nextTwoBytes = new byte[2];
            byte[] nextThreeBytes = new byte[3];
            byte[] nextFourBytes = new byte[4];

            detectEOS(is.read(nextThreeBytes));
            // baos.write(nextThreeBytes);
            frame.payloadLength = ByteUtils.getIntFromNBytes(nextThreeBytes, 0, 3);

            is.read(nextTwoBytes);
            // baos.write(nextTwoBytes);
            frame.type = nextTwoBytes[0];
            frame.flags = nextTwoBytes[1];

            is.read(nextFourBytes);
            // baos.write(nextFourBytes);
            //reserved bit is always 0x0, it's fine
            frame.streamIdentifier = ByteUtils.getIntFrom4Bytes(nextFourBytes, 0);

            if(frame.payloadLength > 0){
                byte[] payloadBytes = new byte[frame.payloadLength];
                attemptToFillWholeBuffer(payloadBytes, is, frame.payloadLength);

                FrameTypes frameType = FrameTypes.fromByte(frame.type);
                if(frameType == null)
                    throw new InvalidFrameTypeException("Frame type of value " + frame.type  + " is invalid");
                // ByteArrayInputStream is used because it's size is fixed.
                // It is easier to parse the frame.
                frame.payload = frameType.parseFunc.apply(frame, new ByteArrayInputStream(payloadBytes), hpackDecoder);
            }else if(frame.payloadLength > config.getMaxFrameSize()){
                frame.invalid();
                // handle errors... (for more, please read the docs)
            }
        }catch(IOException | UncheckedIOException e){
            throw new IOException(e);
        }
        return frame;
    }

    /**
     * Attempts to fill the whole buffer by reading the stream ONCE more. This is a
     * BEST EFFORT operation. It is not guranteed to be filled.
     * @return number of bytes read (This SHOULD be expectedLength == totalReadCount)
     */
    private int attemptToFillWholeBuffer(byte[] byteArray, InputStream is, int expectedLength) throws IOException{
        int attemptedReadByteCount = is.read(byteArray);
        int nextReadCount = 0;
        if(attemptedReadByteCount != expectedLength){
            nextReadCount = is.read(byteArray, attemptedReadByteCount, expectedLength-attemptedReadByteCount);
        }
        // WebServer.logger.warn("Index of null byte: " + ByteUtils.indexOf(byteArray, (byte)0));
        // WebServer.logger.warn("First Attempt: " + attemptedReadByteCount + "/" + expectedLength);
        // WebServer.logger.warn("Last  Attempt: " + (attemptedReadByteCount + nextReadCount) + "/" + expectedLength);
        return attemptedReadByteCount + nextReadCount;
    }

    /**
     * End of Stream
     */
    private void detectEOS(int bytesRead) throws IOException{
        if(bytesRead == -1) throw new IOException("End of stream reached.");
    }
}
