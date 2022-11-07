package com.vincentcodes.webserver.helper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Uses BufferedInputStream as the underlying implmentation
 * which increases performance a lot.
 */
public class TextBinaryInputStream extends InputStream{
    private InputStream is;

    /**
     * <b>Caution!</b>
     * <p>
     * By using this class, it will automatically convert InputStream 
     * into BufferedInputStream if it is not one. Be very careful 
     * when you use this class.
     * @param is
     */
    public TextBinaryInputStream(InputStream is){
        if(!(is instanceof BufferedInputStream)){
            BufferedInputStream bis = new BufferedInputStream(is);
            this.is = bis;
        }else{
            this.is = is;
        }
    }
    public TextBinaryInputStream(BufferedInputStream bis){
        is = bis;
    }

    /**
     * @return the next byte of data, or -1 if the end of the stream is reached
     */
    @Override
    public int read() throws IOException {
        return is.read();
    }
    
    /**
     * Ignores Carriage Return ('\r') by default.
     * @return a string read with '\n' consumed. It is decoded using 
     * the current platform's default charset. Null if EOF is reached.
     */
    public String readLine() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int byteRead = read();
        if(byteRead == -1) 
            return null;
        for(;;byteRead = read()){
            if(byteRead == -1) 
                return new String(baos.toByteArray()).intern();
            if(byteRead != '\r' && byteRead != '\n')
                baos.write(byteRead);
            if(byteRead == '\n')
                break;
        }
        return new String(baos.toByteArray()).intern();
    }

    public String readUntil(char c) throws IOException {
        return readUntil(c, 0);
    }
    /**
     * @param c a character
     * @param skipCount number of skips of character c
     * @return a string read with the last character (c) skipped 
     * and decoded using the current platform's default charset. 
     * Null if EOF has already reached.
     */
    public String readUntil(char c, int skipCount) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int skipped = 0;
        int byteRead = read();
        if(byteRead == -1) 
            return null;
        for(;;byteRead = read()){
            if(byteRead == -1) 
                return new String(baos.toByteArray()).intern();
            if(byteRead == c){
                if(skipped == skipCount)
                    break;
                skipped++;
            }
            baos.write(byteRead);
        }
        return new String(baos.toByteArray()).intern();
    }

    /**
     * @return a string read with the matching string (str) excluded 
     * and decoded using the current platform's default charset. 
     * Null if EOF has already reached.
     */
    public String readUntil(String matchingStr) throws IOException {
        byte[] bytes = readBytesUntil(matchingStr);
        return bytes == null? null : new String(bytes).intern();
    }
    /**
     * @return bytes read with the matching string excluded
     * Null if EOF has already reached. If EOF is encountered during 
     * the search, the bytes read in the process will be returned.
     */
    public byte[] readBytesUntil(String matchingStr) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] matchingStrBytes = matchingStr.getBytes();
        int matchingCharAt = 0;
        boolean found = false;
        int byteRead = read();
        if(byteRead == -1)
            return null;
        for(;;byteRead = read()){
            if(byteRead == -1) 
                return baos.toByteArray();
            baos.write(byteRead);
            if(byteRead == matchingStrBytes[matchingCharAt]){
                matchingCharAt++;
                if(matchingCharAt == matchingStrBytes.length)
                    found = true;
            }else{
                matchingCharAt = 0;
            }
            if(found){
                // Since binary is not allowed while HTTP runs on UTF-8 text
                byte[] result = baos.toByteArray();
                int strLength = matchingStr.getBytes().length;
                return Arrays.copyOfRange(result, 0, result.length - strLength);
            }
        }
    }
}
