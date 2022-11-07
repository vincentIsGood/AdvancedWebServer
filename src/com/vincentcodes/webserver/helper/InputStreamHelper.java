package com.vincentcodes.webserver.helper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Very similar to DataInputStream (I messed up)
 */
public class InputStreamHelper {
    private byte[] twoByteBuffer = new byte[2];
    private byte[] fourByteBuffer = new byte[4];
    private InputStream is;

    public InputStreamHelper(InputStream is){
        this.is = is;
    }

    public void set(InputStream is){
        this.is = is;
    }

    public InputStream get(){
        return is;
    }

    public byte[] getNextTwoBytes() throws IOException{
        is.read(twoByteBuffer);
        return twoByteBuffer;
    }

    public byte[] getNextFourBytes() throws IOException{
        is.read(fourByteBuffer);
        return fourByteBuffer;
    }

    public byte[] getNextNBytes(int n) throws IOException{
        byte[] buffer = new byte[n];
        is.read(buffer);
        return buffer;
    }

    public void clearBuffer(){
        twoByteBuffer = new byte[2];
        fourByteBuffer = new byte[4];
    }
    
}
