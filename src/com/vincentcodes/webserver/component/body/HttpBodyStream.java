package com.vincentcodes.webserver.component.body;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import com.vincentcodes.webserver.component.header.EntityEncodings;

public class HttpBodyStream implements HttpBody {
    /**
     * The whole body will be stored here in bytes.
     */
    private ByteArrayOutputStream byteArray;
    private OutputStream os; // compression output stream
    private EntityEncodings encoding;
    private int maxCap = -1;
    private int writtenCount = 0;
    private int nextByteIndex = 0;
    
    public HttpBodyStream(ByteArrayOutputStream byteArray, EntityEncodings encoding){
        this.byteArray = byteArray;
        os = byteArray;
        try{
            if(encoding == EntityEncodings.GZIP){
                this.encoding = encoding;
                os = new GZIPOutputStream(byteArray);
            }else if(encoding == EntityEncodings.DEFLATE){
                this.encoding = encoding;
                os = new DeflaterOutputStream(byteArray);
            }else{
                this.encoding = null;
            }
        }catch(IOException ignored){}
    }
    public HttpBodyStream(EntityEncodings encoding){
        this(new ByteArrayOutputStream(), encoding);
    }
    public HttpBodyStream(){
        this(new ByteArrayOutputStream(), null);
    }

    @Override
    public void maxCapacity(int maxCap){
        this.maxCap = maxCap;
    }

    @Override
    public EntityEncodings getAcceptedEncoding(){
        return encoding;
    }

    @Override
    public void writeToBody(int b) throws IOException{
        if(maxCap != -1){
            if(writtenCount > maxCap) return;
            writtenCount += 1;
        }
        os.write(b);
    }

    @Override
    public void writeToBody(byte[] b) throws IOException{
        if(maxCap != -1){
            if(writtenCount > maxCap) return;
            writtenCount += b.length;
        }
        os.write(b);
    }
    
    @Override
    public String string(){
        if(encoding != null){
            return null;
        }
        return byteArray.toString();
    }

    /**
     * Once this method is invoked, {@link DeflaterOutputStream#finish()} is invoked
     * inside the method to indicate end of compression. 
     * 
     * This method is also used by {@link #length()}.
     */
    @Override
    public byte[] getBytes(){
        if(encoding != null){
            try{
                ((DeflaterOutputStream)os).finish();
            }catch(IOException ignored){}
        }
        return byteArray.toByteArray();
    }

    /**
     * Will call {@link #getBytes} for each call. Not recommended if you wanna do 
     * this repeatedly.
     * @return empty byte[] if end-of-stream is already reached.
     */
    @Override
    public byte[] getBytes(int length){
        byte[] bytes = getBytes();
        if(nextByteIndex >= bytes.length)
            return new byte[0];
        return Arrays.copyOfRange(bytes, nextByteIndex, Math.min(nextByteIndex += length, bytes.length));
    }
    
    /**
     * This method uses {@link #getBytes()}, which will finish off the
     * compression immediately. 
     */
    @Override
    public void streamBytesTo(OutputStream os) throws IOException {
        os.write(getBytes());
    }

    /**
     * This method uses {@link #getBytes()}, which will finish off the
     * compression immediately. 
     * @return Length of the buffer (if compression is used, then it's 
     * the compressed size)
     */
    @Override
    public int length(){
        return getBytes().length;
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
    
    public void resetInput() throws IOException{
        nextByteIndex = 0;
    }
}
