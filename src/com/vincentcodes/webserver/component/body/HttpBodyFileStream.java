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
    private final File file;
    private final boolean deleteOnClose;
    private long lengthToRead;
    private long readCount;

    private FileOutputStream fos;
    private FileInputStream fis;
    private long maxCap = -1;
    private long writtenCount = 0;
    
    public HttpBodyFileStream(File file, long lengthToRead, boolean deleteOnClose, boolean readonly) throws FileNotFoundException{
        this.file = file;
        this.lengthToRead = lengthToRead;
        this.deleteOnClose = deleteOnClose;
        if(deleteOnClose) 
            file.deleteOnExit();
        
        if(!readonly)
            fos = new FileOutputStream(file);
        fis = new FileInputStream(file);
    }
    public HttpBodyFileStream(File file, long lengthToRead) throws FileNotFoundException{
        this(file, lengthToRead, false, true);
    }
    /**
     * Creates a temporary file to store and get the data
     */
    public HttpBodyFileStream() throws IOException{
        this(File.createTempFile("webserver-res", null), 0, true, false);
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
        if(fos == null) 
            throw new IOException("This HttpBody is not configured to allow write operations on the file.");
        if(maxCap != -1){
            if(writtenCount > maxCap) return;
        }
        writtenCount += 1;
        lengthToRead = writtenCount;
        fos.write(b);
    }

    @Override
    public void writeToBody(byte[] b) throws IOException{
        writeToBody(b, b.length);
    }
    
    @Override
    public void writeToBody(byte[] b, int length) throws IOException {
        if(fos == null) 
            throw new IOException("This HttpBody is not configured to allow write operations on the file.");
        if(maxCap != -1){
            if(writtenCount > maxCap) return;
        }
        writtenCount += length;
        lengthToRead = writtenCount;
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

    @Override
    public int available() throws IOException{
        return fis.available();
    }

    /**
     * Once this method is invoked, {@link DeflaterOutputStream#finish()} is invoked
     * inside the method to indicate end of compression.
     */
    @Override
    public byte[] getBytes(){
        if(readCount >= lengthToRead){
            return new byte[0];
        }
        try{
            readCount += lengthToRead;
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
        if(readCount >= lengthToRead){
            return new byte[0];
        }
        try{
            readCount += length;
            return fis.readNBytes(readCount > lengthToRead? (int)(lengthToRead - (readCount - length)) : length);
        }catch(IOException e){
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * @return -1 while error or eof is reached
     */
    @Override
    public int getBytes(byte[] buffer){
        if(readCount >= lengthToRead){
            return -1;
        }
        try{
            int bytesRead = fis.read(buffer);
            readCount += bytesRead; 
            return bytesRead;
        }catch(IOException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * length will not apply here.
     */
    @Override
    public void streamBytesTo(OutputStream os) throws IOException {
        fis.transferTo(os);
    }

    /**
     * @return available bytes up for grab
     */
    @Override
    public long length(){
        return lengthToRead;
    }

    @Override
    public void close() throws IOException {
        try{
            fis.close();
            if(fos != null)
                fos.close();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(deleteOnClose)
                file.delete();
        }
    }

    /**
     * Skip number of bytes for FileInputStream
     * @throws IOException
     */
    public void skip(long numOfBytes) throws IOException{
        fis.skip(numOfBytes);
    }

    /**
     * When error occurs, input stream will NOT be reset.
     */
    public void resetInput() throws IOException{
        fis.close();
        fis = new FileInputStream(file);
        readCount = 0;
    }
}
