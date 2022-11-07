package com.vincentcodes.webserver.http2.hpack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.vincentcodes.webserver.http2.DynamicTable;
import com.vincentcodes.webserver.http2.Http2TableEntry;
import com.vincentcodes.webserver.http2.StaticTable;
import com.vincentcodes.webserver.http2.constants.CanonicalHuffmanCode;

/**
 * This util is used specifically for HPACK, so it will not be put into util package.
 */
public class HpackCodecUtils {
    /////////////////
    // For encoder //
    /////////////////
    /**
     * @see https://tools.ietf.org/html/rfc7541#section-5.2
     */
    public static byte[] encodeString(String str){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            byte[] utf8encoded = str.getBytes(StandardCharsets.UTF_8);
            os.write(encodeInteger(0, utf8encoded.length, 7)); // 0, not huffman-encoded
            os.write(utf8encoded);
        }catch(IOException e){}
        return os.toByteArray();
    }
    public static byte[] encodeStringHuffman(String str){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            byte[] utf8encoded = CanonicalHuffmanCode.encode(str);
            os.write(encodeInteger(1, utf8encoded.length, 7));
            os.write(utf8encoded);
        }catch(IOException e){}
        return os.toByteArray();
    }

    /**
     * Integer Representation
     * Pseudocode to represent an integer I is as follows:
     * 
     * if I < 2^N - 1, encode I on N bits
     * else
     *     encode (2^N - 1) on N bits
     *     I = I - (2^N - 1)
     *     while I >= 128
     *             encode (I % 128 + 128) on 8 bits
     *             I = I / 128
     *     encode I on 8 bits
     * 
     * @param suffix you have n-bit prefix, the (8-n) bit suffix 
     * is needed here to complete a full byte (eg. xx111111, 
     * xx is the suffix this method needs). 
     * <p>
     * Note that there will
     * not be any error handling (eg. if xx is larger than 4, 
     * no error will occur)
     * @param num number to be encoded
     * @param n N-bit prefix
     * @see https://tools.ietf.org/html/rfc7541#section-5.1
     */
    public static byte[] encodeInteger(int suffix, int num, int n){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        if(num < Math.pow(2, n)-1){
            os.write(num | (suffix << n));
        }else{
            os.write((int)Math.pow(2, n)-1 | (suffix << n));
            num -= (int)Math.pow(2, n)-1;
            while(num >= 128){
                os.write(num % 128 + 128);
                num /= 128;
            }
            os.write(num);
        }
        return os.toByteArray();
    }
    public static byte[] encodeInteger(int num, int n){
        return encodeInteger(0, num, n);
    }

    
    /////////////////
    // For decoder //
    /////////////////
    /**
     * String Literal (huffman code is not yet implemented; ignores it)
     * @param is [will be modified]
     * @see https://tools.ietf.org/html/rfc7541#section-5.2
     */
    public static String decodeString(InputStream is) throws IOException{
        int[] result = decodeInteger(is, 7);
        int isHuffmanCodeEnabled = result[0];
        int length = result[1];
        byte[] data = new byte[length];
        is.read(data);
        if(isHuffmanCodeEnabled == 1){
            return CanonicalHuffmanCode.decode(data);
        }
        return new String(data).intern();
    }
    /**
     * N is the N-bit prefix.
     * Pseudocode to decode an integer I is as follows:
     * 
     * decode I from the next N bits
     * if I < 2^N - 1, return I
     * else
     *     M = 0
     *     repeat
     *         B = next octet
     *         I = I + (B & 127) * 2^M
     *         M = M + 7
     *     while B & 128 == 128
     *     return I
     * 
     * @param firstByte once the first byte has been read, this method must 
     * be used. Otherwise, please refer to {@link HpackCodecUtils#decodeInteger(InputStream, int)} 
     * @param is [will be modified] The input stream which starting from the 
     * next byte it will be decoded as an integer
     * @param n N-bit prefix
     * @return [suffixVal, decoded integer]
     * <p>eg. For suffixVal, 1 when leftmost bit is turned on with N=7
     * @see https://tools.ietf.org/html/rfc7541#section-5.1
     */
    public static int[] decodeInteger(int firstByte, InputStream is, int n) throws IOException{
        int[] twoResult = new int[2];
        int mask = getBigEdianMask(n);
        // int firstByte = is.read();
        int i = firstByte & mask;
        twoResult[0] = (firstByte & (255-mask)) >> n;
        if(i < Math.pow(2, n)-1){
            twoResult[1] = i;
            return twoResult;
        }
        int m = 0;
        int b = 0;
        do{
            b = is.read();
            i += (b & 127) * (int)Math.pow(2, m);
            m += 7;
        }while((b & 128) == 128);
        twoResult[1] = i;
        return twoResult;
    }
    public static int[] decodeInteger(InputStream is, int n) throws IOException{
        return decodeInteger(is.read(), is, n);
    }
    public static int[] decodeInteger(byte[] bytes, int n) throws IOException{
        return decodeInteger(new ByteArrayInputStream(bytes), n);
    }

    /**
     * @param n 1-8
     */
    private static int getBigEdianMask(int n){
        switch(n){
            case 1: return 1;
            case 2: return 3;
            case 3: return 7;
            case 4: return 15;
            case 5: return 31;
            case 6: return 63;
            case 7: return 127;
            case 8: return 255;
        }
        return -1;
        // I would rather not use for loop for that.
        // int mask = 0;
        // for(int i = 0; i < n; i++){
        //     mask |= 1 << i;
        // }
        // return mask;
    }

    /////////////////
    //  For  both  //
    /////////////////
    /**
     * Searches StaticTable automatically
     */
    public static Optional<Http2TableEntry> findEntryFromBothTables(DynamicTable dynamicTable, int index){
        Http2TableEntry result;
        if((result = StaticTable.instance.getEntry(index)) != null){
            return Optional.of(result);
        }else if((result = dynamicTable.getEntry(index)) != null){
            return Optional.of(result);
        }
        return Optional.empty();
    }
    public static Optional<Integer> findEntryIndexFromBothTables(DynamicTable dynamicTable, String name){
        int result = 0;
        if((result = StaticTable.instance.getFirstEntryIndex(name)) != -1){
            return Optional.of(result);
        }else if((result = dynamicTable.getFirstEntryIndex(name)) != -1){
            return Optional.of(result);
        }
        return Optional.empty();
    }
    public static Optional<Integer> findEntryIndexFromBothTables(DynamicTable dynamicTable, String name, String value){
        int result = 0;
        if((result = StaticTable.instance.getFirstEntryIndex(name, value)) != -1){
            return Optional.of(result);
        }else if((result = dynamicTable.getFirstEntryIndex(name, value)) != -1){
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
