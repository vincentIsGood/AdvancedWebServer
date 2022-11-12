package com.vincentcodes.webserver.component.body;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import com.vincentcodes.webserver.component.header.EntityEncodings;

/**
 * Does not apply any archiving, compression to the bytes 
 */
public class HttpBodyFileStream implements HttpBody {
    private File file;
    private boolean deleteOnClose;

    private FileOutputStream fos;
    private FileInputStream fis;
    private int maxCap = -1;
    private int writtenCount = 0;
    
    public HttpBodyFileStream(File file, boolean deleteOnClose) throws FileNotFoundException{
        this.file = file;
        this.deleteOnClose = deleteOnClose;
        if(deleteOnClose) 
            file.deleteOnExit();
        
        fos = new FileOutputStream(file);
        fis = new FileInputStream(file);
    }
    public HttpBodyFileStream(File file) throws FileNotFoundException{
        this(file, false);
    }
    /**
     * Creates a temporary file to store and get the data
     */
    public HttpBodyFileStream() throws IOException{
        this(File.createTempFile("webserver-res", null), true);
    }

    @Override
    public void maxCapacity(int maxCap){
        this.maxCap = maxCap;
    }

    /**
     * @return null
     */
    @Override
    public EntityEncodings getAcceptedEncoding(){
        return null;
    }

    @Override
    public void writeToBody(int b) throws IOException{
        if(maxCap != -1){
            if(writtenCount > maxCap) return;
            writtenCount += 1;
        }
        fos.write(b);
    }

    @Override
    public void writeToBody(byte[] b) throws IOException{
        writeToBody(b, b.length);
    }
    
    @Override
    public void writeToBody(byte[] b, int length) throws IOException {
        if(maxCap != -1){
            if(writtenCount > maxCap) return;
            writtenCount += length;
        }
        fos.write(b, 0, length);
    }

    /**
     * Will call {@link #resetInput}.
     */
    @Override
    public String string(){
        String result = new String(getBytes());
        try {
            resetInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Once this method is invoked, {@link DeflaterOutputStream#finish()} is invoked
     * inside the method to indicate end of compression. 
     * 
     * This method is also used by {@link #length()}.
     */
    @Override
    public byte[] getBytes(){
        try{
            return fis.readAllBytes();
        }catch(IOException e){
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    /**
     * It's not recommended to use this method after {@link #getBytes}, which
     * reads all bytes from the stream.
     * @return empty byte[] is returned, when end-of-stream is already reached
     */
    @Override
    public byte[] getBytes(int length) {
        try{
            return fis.readNBytes(length);
        }catch(IOException e){
            e.printStackTrace();
            return new byte[0];
        }
    }

    @Override
    public void streamBytesTo(OutputStream os) throws IOException {
        fis.transferTo(os);
    }

    /**
     * @return available bytes up for grab
     */
    @Override
    public int length(){
        try {
            return fis.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void close() throws IOException {
        try{
            fis.close();
            fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(deleteOnClose)
                file.delete();
        }
    }

    /**
     * When error occurs, input stream will NOT be reset.
     */
    public void resetInput() throws IOException{
        fis.close();
        fis = new FileInputStream(file);
    }
}
