package com.vincentcodes.webserver.http2.hpack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.vincentcodes.webserver.http2.DynamicTable;
import com.vincentcodes.webserver.http2.Http2Configuration;
import com.vincentcodes.webserver.http2.Http2TableEntry;
import com.vincentcodes.webserver.http2.StaticTable;

/**
 * For the specifications see {@link com.vincentcodes.
 * webserver.http2.hpack.HpackDecoder here}
 */
public class HpackEncoder {
    private DynamicTable dynamicTable;

    /**
     * Create HpackDecoder using default configuration/settings of a stream
     */
    public HpackEncoder(){
        this.dynamicTable = new DynamicTable(new Http2Configuration(), StaticTable.instance.size()+1);
    }
    public HpackEncoder(DynamicTable dynamicTable){
        this.dynamicTable = dynamicTable;
    }

    /**
     * @see https://tools.ietf.org/html/rfc7541#section-6
     */
    public byte[] encode(List<Http2TableEntry> headers, boolean huffmanEncoded){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            byte[] bytes;
            for(Http2TableEntry header : headers){
                if((bytes = encodeIndexedHeaderField(header)) != null){
                    os.write(bytes);
                }else{
                    os.write(encodeLiteralWithoutIndexing(header, huffmanEncoded));
                }
            }
        }catch(IOException e){}
        return os.toByteArray();
    }

    /**
     * Not recommended to alter the dynamic table
     */
    public byte[] encodeWithIncremental(List<Http2TableEntry> headers, boolean huffmanEncoded){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            byte[] bytes;
            for(Http2TableEntry header : headers){
                if((bytes = encodeIndexedHeaderField(header)) != null){
                    os.write(bytes);
                }else{
                    os.write(encodeLiteralWithIndexing(header, huffmanEncoded));
                }
            }
        }catch(IOException e){}
        return os.toByteArray();
    }
    
    // Encode Header Fields
    public byte[] encodeIndexedHeaderField(Http2TableEntry entry){
        Optional<Integer> entryIndex = HpackCodecUtils.findEntryIndexFromBothTables(dynamicTable, entry.getName(), entry.getValue());
        if(!entryIndex.isPresent()){
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            os.write(HpackCodecUtils.encodeInteger(1, entryIndex.get(), 7));
        }catch(IOException e){}
        return os.toByteArray();
    }

    /**
     * Not recommended to alter the dynamic table
     */
    public byte[] encodeLiteralWithIndexing(Http2TableEntry entry, boolean huffmanEncoded){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            Optional<Integer> entryIndex = HpackCodecUtils.findEntryIndexFromBothTables(dynamicTable, entry.getName());
            if(entryIndex.isPresent()){
                os.write(HpackCodecUtils.encodeInteger(1, entryIndex.get(), 6));
                if(!huffmanEncoded) os.write(HpackCodecUtils.encodeString(entry.getValue()));
                else os.write(HpackCodecUtils.encodeStringHuffman(entry.getValue()));
                dynamicTable.addHeader(entry);
            }else{
                os.write(HpackCodecUtils.encodeInteger(1, 0, 6));
                if(!huffmanEncoded){
                    os.write(HpackCodecUtils.encodeString(entry.getName()));
                    os.write(HpackCodecUtils.encodeString(entry.getValue()));
                }else{
                    os.write(HpackCodecUtils.encodeStringHuffman(entry.getName()));
                    os.write(HpackCodecUtils.encodeStringHuffman(entry.getValue()));
                }
                dynamicTable.addHeader(entry);
            }
        }catch(IOException e){}
        return os.toByteArray();
    }

    public byte[] encodeLiteralWithoutIndexing(Http2TableEntry entry, boolean huffmanEncoded){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            Optional<Integer> entryIndex = HpackCodecUtils.findEntryIndexFromBothTables(dynamicTable, entry.getName());
            if(entryIndex.isPresent()){
                os.write(HpackCodecUtils.encodeInteger(entryIndex.get(), 4));
                if(!huffmanEncoded) os.write(HpackCodecUtils.encodeString(entry.getValue()));
                else os.write(HpackCodecUtils.encodeStringHuffman(entry.getValue()));
            }else{
                os.write(0);
                if(!huffmanEncoded){
                    os.write(HpackCodecUtils.encodeString(entry.getName()));
                    os.write(HpackCodecUtils.encodeString(entry.getValue()));
                }else{
                    os.write(HpackCodecUtils.encodeStringHuffman(entry.getName()));
                    os.write(HpackCodecUtils.encodeStringHuffman(entry.getValue()));
                }
            }
        }catch(IOException e){}
        return os.toByteArray();
    }
    
    public byte[] encodeLiteralNeverIndexed(Http2TableEntry entry){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            Optional<Integer> entryIndex = HpackCodecUtils.findEntryIndexFromBothTables(dynamicTable, entry.getName());
            if(entryIndex.isPresent()){
                os.write(HpackCodecUtils.encodeInteger(1, entryIndex.get(), 4));
                os.write(HpackCodecUtils.encodeString(entry.getValue()));
            }else{
                os.write(HpackCodecUtils.encodeInteger(1, 0, 4));
                os.write(HpackCodecUtils.encodeString(entry.getName()));
                os.write(HpackCodecUtils.encodeString(entry.getValue()));
            }
        }catch(IOException e){}
        return os.toByteArray();
    }
}
