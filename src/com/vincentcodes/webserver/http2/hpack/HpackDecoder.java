package com.vincentcodes.webserver.http2.hpack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.vincentcodes.webserver.http2.DynamicTable;
import com.vincentcodes.webserver.http2.Http2Configuration;
import com.vincentcodes.webserver.http2.Http2TableEntry;
import com.vincentcodes.webserver.http2.StaticTable;
import com.vincentcodes.webserver.http2.errors.DecodeError;

/**
 * <p>
 * HPACK is a compression format for efficiently 
 * representing HTTP header fields, to be used in HTTP/2.
 * <p>
 * A header block is the concatenation of header field 
 * representations.
 * 
 * @see https://tools.ietf.org/html/rfc7541#section-2
 * @see https://tools.ietf.org/html/rfc7541#section-3.1
 */
public class HpackDecoder {
    private DynamicTable dynamicTable;

    /**
     * Create HpackDecoder using default configuration/settings of a stream
     */
    public HpackDecoder(){
        this.dynamicTable = new DynamicTable(new Http2Configuration(), StaticTable.instance.size()+1);
    }
    public HpackDecoder(DynamicTable dynamicTable){
        this.dynamicTable = dynamicTable;
    }

    public List<Http2TableEntry> decode(InputStream is) throws IOException{
        List<Http2TableEntry> list = new ArrayList<>();
        Http2TableEntry entry;
        while((entry = decodeHeaderModifyTable(is)) != null){
            list.add(entry);
        }
        return list;
    }
    public List<Http2TableEntry> decode(byte[] bytes) throws IOException{
        return decode(new ByteArrayInputStream(bytes));
    }

    /**
     * @param is prefers ByteArrayInputStream (since it has EOF)
     * @see https://tools.ietf.org/html/rfc7541#section-6
     */
    private Http2TableEntry decodeHeaderModifyTable(InputStream is) throws IOException{
        int identifierByte = is.read();
        if(identifierByte == -1)
            return null;
        
        if((identifierByte & 0b10000000) == 128){
            return decodeIndexedHeaderField(identifierByte, is);
        }else if((identifierByte & 0b11000000) == 0b01000000){
            return decodeLiteralWithIndexing(identifierByte, is);
        }else if((identifierByte & 0b11110000) == 0){
            return decodeLiteralWithoutIndexing(identifierByte, is);
        }else if((identifierByte & 0b11110000) == 0b00010000){
            return decodeLiteralNeverIndexed(identifierByte, is);
        }else if((identifierByte & 0b11100000) == 0b00100000){
            // 6.3 Dynamic Table Size Update
            int maxSize = HpackCodecUtils.decodeInteger(identifierByte, is, 5)[1];
            if(maxSize > dynamicTable.getConfig().getMaxHeaderListSize()){
                throw new DecodeError("Cannot update table size: It's over the limit of " + dynamicTable.getConfig().getMaxHeaderListSize());
            }
            dynamicTable.trimToSize(maxSize);
            // this update frame may cause problems
        }
        throw new DecodeError("Invalid header");
    }

    /**
     * 6.1 Indexed Header Field
     */
    private Http2TableEntry decodeIndexedHeaderField(int identifierByte, InputStream is) throws IOException{
        int index = HpackCodecUtils.decodeInteger(identifierByte, is, 7)[1];
        Optional<Http2TableEntry> entry = HpackCodecUtils.findEntryFromBothTables(dynamicTable, index);
        if(!entry.isPresent()){
            throw new DecodeError(notFoundErrorString(index));
        }
        return entry.get();
    }

    /**
     * 6.2.1 Literal Header Field With Incremental Indexing
     */
    private Http2TableEntry decodeLiteralWithIndexing(int identifierByte, InputStream is) throws IOException{
        int index = HpackCodecUtils.decodeInteger(identifierByte, is, 6)[1];
        if(index != 0){
            Optional<Http2TableEntry> entry = HpackCodecUtils.findEntryFromBothTables(dynamicTable, index);
            if(!entry.isPresent()){
                throw new DecodeError(notFoundErrorString(index));
            }
            return dynamicTable.addHeader(entry.get().getName(), HpackCodecUtils.decodeString(is));
        }
        return dynamicTable.addHeader(HpackCodecUtils.decodeString(is), HpackCodecUtils.decodeString(is));
    }

    /**
     * 6.2.2 Literal Header Field without Indexing
     */
    private Http2TableEntry decodeLiteralWithoutIndexing(int identifierByte, InputStream is) throws IOException{
        int index = HpackCodecUtils.decodeInteger(identifierByte, is, 4)[1];
        if(index != 0){
            Optional<Http2TableEntry> entry = HpackCodecUtils.findEntryFromBothTables(dynamicTable, index);
            if(!entry.isPresent()){
                throw new DecodeError(notFoundErrorString(index));
            }
            return new Http2TableEntry(entry.get().getName(), HpackCodecUtils.decodeString(is));
        }
        return new Http2TableEntry(HpackCodecUtils.decodeString(is), HpackCodecUtils.decodeString(is));
    }

    /**
     * 6.2.3 Literal Header Field Never Indexed
     */
    private Http2TableEntry decodeLiteralNeverIndexed(int identifierByte, InputStream is) throws IOException{
        return decodeLiteralWithoutIndexing(identifierByte, is);
    }

    private String notFoundErrorString(int fetchingIndex){
        return "Entry not found with index: " + fetchingIndex + "; Dynamic Table Range: " + dynamicTable.getBaseIndex() + "-" + (dynamicTable.getBaseIndex()+dynamicTable.size());
    }
}
