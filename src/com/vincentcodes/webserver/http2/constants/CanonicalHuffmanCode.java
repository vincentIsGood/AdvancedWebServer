package com.vincentcodes.webserver.http2.constants;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * @see https://tools.ietf.org/html/rfc7541#appendix-B
 */
public class CanonicalHuffmanCode {
    private final static Map<Integer, String> symToCode = new HashMap<>();
    private final static Map<String, Integer> codeToSymSpecial = new HashMap<>(); // with leading zeros
    private final static Map<Integer, Integer> codeToSym = new HashMap<>();

    /**
     * @param symbol a value of 0-256 (inclusive)
     */
    public static String getCodeFromSym(int symbol){
        return symToCode.get(symbol);
    }
    
    /**
     * @param binSymbol eg. "100001" gives 'A'
     * @return the corresponding character
     */
    public static Optional<Character> getCharFromBinSym(String binSymbol){
        Integer c = codeToSym.get(Integer.parseInt(binSymbol, 2));
        if(c == null) c = codeToSymSpecial.get(binSymbol);
        if(c == null) return Optional.empty();
        return Optional.of((char)c.intValue());
    }

    /**
     * @return the decoded string
     */
    public static String decode(byte[] buf){
        String combinedBin = "";
        for(byte b : buf){ 
            combinedBin += padStart(Integer.toBinaryString(Byte.toUnsignedInt(b)), 8, "0");
        }
        String result = "";
        Optional<Character> ch;
        // endIndex arg of substring(int, int) is exclusive
        for(int i = 1, j = 0; i <= combinedBin.length(); i++){
            if(i-j > 4){
                if((ch = CanonicalHuffmanCode.getCharFromBinSym(combinedBin.substring(j, i))).isPresent()){
                    j += (i-j);
                    result += ch.get();
                }
            }
        }
        return result;
    }

    /**
     * @return the encoded string in bytes
     */
    public static byte[] encode(String str){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String combinedBin = "";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        for(int i = 0; i < strBytes.length; i++){
            combinedBin += getCodeFromSym(strBytes[i] & 0xff);
        }
        if(combinedBin.length() % 8 != 0){
            // pad the end with '1's, because it's very easy to reach '00000' which is '0'
            combinedBin = padEnd(combinedBin, (int)Math.ceil((double)(combinedBin.length())/8)*8, "1");
        }
        for(int i = 1, j = 0; i <= combinedBin.length(); i++){
            if(i - j >= 8){
                os.write(Integer.parseInt(combinedBin.substring(j, i), 2));
                j += (i - j);
            }
        }
        return os.toByteArray();
    }

    private static String padStart(String target, int maxLength, String padStr){
        while(target.length() < maxLength){
            target = padStr + target;
        }
        return target;
    }
    private static String padEnd(String target, int maxLength, String padStr){
        while(target.length() < maxLength){
            target += padStr;
        }
        return target;
    }

    private static int[] huffmanCodes = new int[]{
        0x1ff8,
        0x7fffd8,
        0xfffffe2,
        0xfffffe3,
        0xfffffe4,
        0xfffffe5,
        0xfffffe6,
        0xfffffe7,
        0xfffffe8,
        0xffffea,
        0x3ffffffc,
        0xfffffe9,
        0xfffffea,
        0x3ffffffd,
        0xfffffeb,
        0xfffffec,
        0xfffffed,
        0xfffffee,
        0xfffffef,
        0xffffff0,
        0xffffff1,
        0xffffff2,
        0x3ffffffe,
        0xffffff3,
        0xffffff4,
        0xffffff5,
        0xffffff6,
        0xffffff7,
        0xffffff8,
        0xffffff9,
        0xffffffa,
        0xffffffb,
        0x14,
        0x3f8,
        0x3f9,
        0xffa,
        0x1ff9,
        0x15,
        0xf8,
        0x7fa,
        0x3fa,
        0x3fb,
        0xf9,
        0x7fb,
        0xfa,
        0x16,
        0x17,
        0x18,
        0x0,
        0x1,
        0x2,
        0x19,
        0x1a,
        0x1b,
        0x1c,
        0x1d,
        0x1e,
        0x1f,
        0x5c,
        0xfb,
        0x7ffc,
        0x20,
        0xffb,
        0x3fc,
        0x1ffa,
        0x21,
        0x5d,
        0x5e,
        0x5f,
        0x60,
        0x61,
        0x62,
        0x63,
        0x64,
        0x65,
        0x66,
        0x67,
        0x68,
        0x69,
        0x6a,
        0x6b,
        0x6c,
        0x6d,
        0x6e,
        0x6f,
        0x70,
        0x71,
        0x72,
        0xfc,
        0x73,
        0xfd,
        0x1ffb,
        0x7fff0,
        0x1ffc,
        0x3ffc,
        0x22,
        0x7ffd,
        0x3,
        0x23,
        0x4,
        0x24,
        0x5,
        0x25,
        0x26,
        0x27,
        0x6,
        0x74,
        0x75,
        0x28,
        0x29,
        0x2a,
        0x7,
        0x2b,
        0x76,
        0x2c,
        0x8,
        0x9,
        0x2d,
        0x77,
        0x78,
        0x79,
        0x7a,
        0x7b,
        0x7ffe,
        0x7fc,
        0x3ffd,
        0x1ffd,
        0xffffffc,
        0xfffe6,
        0x3fffd2,
        0xfffe7,
        0xfffe8,
        0x3fffd3,
        0x3fffd4,
        0x3fffd5,
        0x7fffd9,
        0x3fffd6,
        0x7fffda,
        0x7fffdb,
        0x7fffdc,
        0x7fffdd,
        0x7fffde,
        0xffffeb,
        0x7fffdf,
        0xffffec,
        0xffffed,
        0x3fffd7,
        0x7fffe0,
        0xffffee,
        0x7fffe1,
        0x7fffe2,
        0x7fffe3,
        0x7fffe4,
        0x1fffdc,
        0x3fffd8,
        0x7fffe5,
        0x3fffd9,
        0x7fffe6,
        0x7fffe7,
        0xffffef,
        0x3fffda,
        0x1fffdd,
        0xfffe9,
        0x3fffdb,
        0x3fffdc,
        0x7fffe8,
        0x7fffe9,
        0x1fffde,
        0x7fffea,
        0x3fffdd,
        0x3fffde,
        0xfffff0,
        0x1fffdf,
        0x3fffdf,
        0x7fffeb,
        0x7fffec,
        0x1fffe0,
        0x1fffe1,
        0x3fffe0,
        0x1fffe2,
        0x7fffed,
        0x3fffe1,
        0x7fffee,
        0x7fffef,
        0xfffea,
        0x3fffe2,
        0x3fffe3,
        0x3fffe4,
        0x7ffff0,
        0x3fffe5,
        0x3fffe6,
        0x7ffff1,
        0x3ffffe0,
        0x3ffffe1,
        0xfffeb,
        0x7fff1,
        0x3fffe7,
        0x7ffff2,
        0x3fffe8,
        0x1ffffec,
        0x3ffffe2,
        0x3ffffe3,
        0x3ffffe4,
        0x7ffffde,
        0x7ffffdf,
        0x3ffffe5,
        0xfffff1,
        0x1ffffed,
        0x7fff2,
        0x1fffe3,
        0x3ffffe6,
        0x7ffffe0,
        0x7ffffe1,
        0x3ffffe7,
        0x7ffffe2,
        0xfffff2,
        0x1fffe4,
        0x1fffe5,
        0x3ffffe8,
        0x3ffffe9,
        0xffffffd,
        0x7ffffe3,
        0x7ffffe4,
        0x7ffffe5,
        0xfffec,
        0xfffff3,
        0xfffed,
        0x1fffe6,
        0x3fffe9,
        0x1fffe7,
        0x1fffe8,
        0x7ffff3,
        0x3fffea,
        0x3fffeb,
        0x1ffffee,
        0x1ffffef,
        0xfffff4,
        0xfffff5,
        0x3ffffea,
        0x7ffff4,
        0x3ffffeb,
        0x7ffffe6,
        0x3ffffec,
        0x3ffffed,
        0x7ffffe7,
        0x7ffffe8,
        0x7ffffe9,
        0x7ffffea,
        0x7ffffeb,
        0xffffffe,
        0x7ffffec,
        0x7ffffed,
        0x7ffffee,
        0x7ffffef,
        0x7fffff0,
        0x3ffffee,
        0x3fffffff
    };

    static{
        IntStream asciiNum = IntStream.range(0, 257);
        asciiNum.forEach(num -> {
            symToCode.put(num, Integer.toBinaryString(huffmanCodes[num]));
            codeToSym.put(huffmanCodes[num], num);
        });
        huffmanCodes = null;

        // Commented hexes are code value
        codeToSymSpecial.put("010100", 32); // 0x14
        codeToSymSpecial.put("010101", 37); // 0x15
        codeToSymSpecial.put("010110", 45); // 0x16
        codeToSymSpecial.put("010111", 46); // 0x17
        codeToSymSpecial.put("011000", 47); // 0x18
        codeToSymSpecial.put("00000", 48); // 0x0
        codeToSymSpecial.put("00001", 49); // 0x1
        codeToSymSpecial.put("00010", 50); // 0x2
        codeToSymSpecial.put("011001", 51); // 0x19
        codeToSymSpecial.put("011010", 52); // 0x1a
        codeToSymSpecial.put("011011", 53); // 0x1b
        codeToSymSpecial.put("011100", 54); // 0x1c
        codeToSymSpecial.put("011101", 55); // 0x1d
        codeToSymSpecial.put("011110", 56); // 0x1e
        codeToSymSpecial.put("011111", 57); // 0x1f
        codeToSymSpecial.put("00011", 97); // 0x3
        codeToSymSpecial.put("00100", 99); // 0x4
        codeToSymSpecial.put("00101", 101); // 0x5
        codeToSymSpecial.put("00110", 105); // 0x6
        codeToSymSpecial.put("00111", 111); // 0x7
        codeToSymSpecial.put("01000", 115); // 0x8
        codeToSymSpecial.put("01001", 116); // 0x9

        codeToSymSpecial.forEach((k, v) -> {
            symToCode.put(v, k);
        });

        for(String b : codeToSymSpecial.keySet()){
            int val = Integer.parseInt(b, 2);
            codeToSym.remove(val);
        }
    }
}
