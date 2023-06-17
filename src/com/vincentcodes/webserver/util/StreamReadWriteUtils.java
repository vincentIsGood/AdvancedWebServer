package com.vincentcodes.webserver.util;

import java.io.IOException;
import java.io.InputStream;

public class StreamReadWriteUtils {
    /**
     * Attempts to fill the whole buffer by reading the stream ONCE more. This is a
     * BEST EFFORT operation. It is not guranteed to be filled.
     * @param byteArray [mutates]
     * @return number of bytes read (This SHOULD be expectedLength == totalReadCount)
     */
    public static int attemptToFillWholeBuffer(byte[] byteArray, InputStream is, int expectedLength) throws IOException{
        int attemptedReadByteCount = is.read(byteArray, 0, expectedLength);
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
     * Same as {@code attemptToFillWholeBuffer(byteArray, is, byteArray.length)}
     */
    public static int attemptToFillWholeBuffer(byte[] byteArray, InputStream is) throws IOException{
        return attemptToFillWholeBuffer(byteArray, is, byteArray.length);
    }

    public static void ensureFilledBuffer(byte[] byteArray, InputStream is, int expectedLength) throws IOException{
        is.readNBytes(byteArray, 0, expectedLength);
    }
    public static void ensureFilledBuffer(byte[] byteArray, InputStream is) throws IOException{
        is.readNBytes(byteArray, 0, byteArray.length);
    }

    /**
     * End of Stream by detecting number of bytes read
     */
    public static void detectEOS(int bytesRead) throws IOException{
        if(bytesRead == -1) throw new IOException("End of stream reached.");
    }
}
