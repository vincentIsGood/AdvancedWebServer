package com.vincentcodes.webserver.http2;

import java.nio.charset.StandardCharsets;

/**
 * I considered using Map.Entry<K,V>. But that is
 * too heavy. So I create this readonly Table Entry.
 */
public class Http2TableEntry {
    protected String name;
    protected String value;

    public Http2TableEntry(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName(){
        return name;
    }

    public String getValue(){
        return value;
    }

    public int size(){
        return name.getBytes(StandardCharsets.UTF_8).length + value.getBytes(StandardCharsets.UTF_8).length;
    }

    public String toString(){
        return String.format("{name: %s, value: %s}", name, value);
    }
}
