package com.vincentcodes.webserver.helper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Uses BufferedInputStream as the underlying implmentation
 * which increases performance a lot.
 * 
 * This class is not thread safe. Do not try to access class
 * methods using multiple threads.
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
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
     * @param skipCount number of skips of character c. Ignore count.
     * @return a string read with the last character (c) skipped 
     * and decoded using the current platform's default charset. 
     * Null if EOF has already reached.
     */
    public String readUntil(char c, int skipCount) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
        int skipped = 0;
        int byteRead = read();
        if(byteRead == -1) 
            return null;
        for(;;byteRead = read()){
            if(byteRead == -1) 
                return new String(baos.toByteArray());
            if(byteRead == c){
                if(skipped == skipCount)
                    break;
                skipped++;
            }
            baos.write(byteRead);
        }
        return new String(baos.toByteArray());
    }

    /**
     * @return a string read with the matching string (str) excluded 
     * and decoded using the current platform's default charset. 
     * Null if EOF has already reached.
     */
    public String readUntil(String matchingStr) throws IOException {
        byte[] bytes = readBytesUntil(matchingStr);
        return bytes == null? null : new String(bytes);
    }
    /**
     * @param readSize read number of bytes out of the stream. readSize
     * must be at least the length of matchingStr
     * @return bytes read with the matching string excluded.
     * {@code ReadUntilResult.data} is null if EOF has already reached. 
     * If EOF is encountered during the search, the bytes read in the 
     * process will be returned.
     */
    private ByteArrayOutputStream prevBuf;
    public ReadUntilResult readBytesUntil(String matchingStr, int readSize) throws IOException {
        byte[] matchingStrBytes = matchingStr.getBytes();
        if(readSize <= 0)
            return new ReadUntilResult(readBytesUntilPure(matchingStr), false);
        if(readSize - matchingStrBytes.length < 0)
            throw new IllegalArgumentException("readSize must be at least the length of matchingStr");
        
        int baosSize = readSize + matchingStr.length();
        ByteArrayOutputStream baos;
        if(prevBuf != null) baos = prevBuf;
        else baos = new ByteArrayOutputStream(baosSize);

        int matchingCharAt = 0;
        boolean found = false;
        int byteRead = read();
        if(byteRead == -1)
            return new ReadUntilResult(null, false);
        for(;;byteRead = read()){
            if(byteRead == -1){
                return new ReadUntilResult(baos.toByteArray(), false);
            }
            baos.write(byteRead);
            if(byteRead == matchingStrBytes[matchingCharAt]){
                matchingCharAt++;
                if(matchingCharAt == matchingStrBytes.length)
                    found = true;
            }else{
                matchingCharAt = 0;
            }
            // if we reach the readSize and last char does not match our match char. Leave.
            // eg. ("long sent_nce").readBytesUntil("sente", 6). Dangling "s" (index 5) matches, 
            //     then we keep matching the string "sente". Disjoint "sent_" let us leave the 
            //     func with "long sent_" in the buffer.
            if(matchingCharAt == 0 && baos.size() >= readSize){
                // eg. "long sent_" ret "ong s"; prevBuf = "ent_"
                if(baos.size() > readSize){
                    byte[] largeArray = baos.toByteArray();
                    byte[] result = Arrays.copyOfRange(largeArray, 0, readSize);
                    prevBuf = new ByteArrayOutputStream(baosSize);
                    prevBuf.write(Arrays.copyOfRange(largeArray, readSize, largeArray.length));
                    return new ReadUntilResult(result, false);
                }
                prevBuf = null;
                return new ReadUntilResult(baos.toByteArray(), false);
            }
            if(found){
                // Exclude the matchingStr
                // Since binary is not allowed while HTTP runs on UTF-8 text
                byte[] result = baos.toByteArray();
                int strLength = matchingStr.getBytes().length;
                return new ReadUntilResult(Arrays.copyOfRange(result, 0, result.length - strLength), true);
            }
        }
    }
    private byte[] readBytesUntilPure(String matchingStr) throws IOException {
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
                // Exclude the matchingStr
                // Since binary is not allowed while HTTP runs on UTF-8 text
                byte[] result = baos.toByteArray();
                int strLength = matchingStr.getBytes().length;
                return Arrays.copyOfRange(result, 0, result.length - strLength);
            }
        }
    }

    public byte[] readBytesUntil(String matchingStr) throws IOException {
        return readBytesUntil(matchingStr, -1).data();
    }
}
