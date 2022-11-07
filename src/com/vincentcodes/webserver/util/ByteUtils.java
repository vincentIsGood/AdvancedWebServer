package com.vincentcodes.webserver.util;

import java.nio.ByteBuffer;

public class ByteUtils {
    public static int toUnsignedByte(byte b){
        // equivalent to Byte.toUnsignedInt
        return b & 0xff;
    }

    /**
     * Reads 2 bytes
     */
    public static int getIntFrom2Bytes(byte[] bytes, int startingIndex){
        return ((bytes[startingIndex] & 0xff) << 8) | (bytes[startingIndex+1] & 0xff);
    }

    /**
     * Reads 4 bytes.
     * I can use a for loop for this.
     * @see https://stackoverflow.com/questions/4768933/read-two-bytes-into-an-integer
     */
    public static int getIntFrom4Bytes(byte[] bytes, int startingIndex){
        return ((bytes[startingIndex] & 0xff) << 8*3) | ((bytes[startingIndex+1] & 0xff) << 8*2) | ((bytes[startingIndex+2] & 0xff) << 8) | (bytes[startingIndex+3] & 0xff);
    }

    public static int getIntFromNBytes(byte[] bytes, int startingIndex, int n){
        int result = 0;
        for(int i = 0; i < n; i++){
            result |= ((bytes[startingIndex+i] & 0xff) << 8*(n-i-1));
        }
        return result;
    }

    public static long getLongFrom4Bytes(byte[] bytes, int startingIndex){
        return ((bytes[startingIndex] & 0xff) << 8*3) | ((bytes[startingIndex+1] & 0xff) << 8*2) | ((bytes[startingIndex+2] & 0xff) << 8) | (bytes[startingIndex+3] & 0xff);
    }

    /**
     * eg. 1ff8 is represented as [24, 255] instead of [0, 0, 24, 255];
     * @return minimum bytes representing the integer value
     */
    public static byte[] convertIntToMinBytes(int value){
        if(value < 256){
            return new byte[]{(byte)value};
        }else if(value < 65536){
            return new byte[]{(byte)(value >> 8), (byte)(value)};
        }else if(value < 16777216){
            return new byte[]{(byte)(value >> 16), (byte)(value >> 8), (byte)value};
        }else{
            return intToByteArray(value);
        }
    }

    // 8 bytes
    public static byte[] longToByteArray(long value){
        // Inconsistency in code... I do it for learning purposes.
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(value);
        return buffer.array();
    }

    // numbers are in form of 0x00000001, so the array is in bytes[]{00,00,00,01}
    // 4 bytes
    public static byte[] intToByteArray(int value){
        return new byte[]{(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value};
    }

    // 2 bytes
    public static byte[] shortToByteArray(short value){
        return new byte[]{(byte)(value >> 8), (byte)value};
    }

    public static int indexOf(byte[] byteArray, byte searchFor){
        for(int i = 0; i < byteArray.length; i++){
            if(byteArray[i] == searchFor) return i;
        }
        return -1;
    }
}
