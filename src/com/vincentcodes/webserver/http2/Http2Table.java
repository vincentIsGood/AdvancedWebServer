package com.vincentcodes.webserver.http2;

public interface Http2Table {
    int getBaseIndex();

    Http2TableEntry getEntry(int index);

    Http2TableEntry getFirstEntry(String name);

    Http2TableEntry getFirstEntry(String name, String value);
    
    int getFirstEntryIndex(String name);

    int getFirstEntryIndex(String name, String value);

    default boolean hasHeader(int index){
        return getEntry(index) != null;
    };

    default boolean hasHeader(String name){
        return getFirstEntry(name) != null;
    };
}
