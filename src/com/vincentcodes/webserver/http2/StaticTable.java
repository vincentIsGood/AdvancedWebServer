package com.vincentcodes.webserver.http2;

import java.util.ArrayList;
import java.util.Optional;

public final class StaticTable implements Http2Table{
    private ArrayList<Http2TableEntry> array;
    private int baseIndex = 1;

    private StaticTable(){
        array = new ArrayList<>();
    }

    public static StaticTable instance = new StaticTable();
    static {
        // :authority (is equivalent to http/1.1 "Host" header)
        instance.appendHeader(":authority"); // index = 1
        instance.appendHeader(":method", "GET");
        instance.appendHeader(":method", "POST");
        instance.appendHeader(":path", "/");
        instance.appendHeader(":path", " /index.html");
        instance.appendHeader(":scheme", "http");
        instance.appendHeader(":scheme", "https");
        instance.appendHeader(":status", "200");
        instance.appendHeader(":status", "204");
        instance.appendHeader(":status", "206");
        instance.appendHeader(":status", "304");
        instance.appendHeader(":status", "400");
        instance.appendHeader(":status", "404");
        instance.appendHeader(":status", "500");
        instance.appendHeader("accept-charset");
        instance.appendHeader("accept-encoding", "gzip, deflate");
        instance.appendHeader("accept-language");
        instance.appendHeader("accept-ranges");
        instance.appendHeader("accept");
        instance.appendHeader("access-control-allow-origin");
        instance.appendHeader("age");
        instance.appendHeader("allow");
        instance.appendHeader("authorization");
        instance.appendHeader("cache-control");
        instance.appendHeader("content-disposition");
        instance.appendHeader("content-encoding");
        instance.appendHeader("content-language");
        instance.appendHeader("content-length");
        instance.appendHeader("content-location");
        instance.appendHeader("content-range");
        instance.appendHeader("content-type");
        instance.appendHeader("cookie");
        instance.appendHeader("date");
        instance.appendHeader("etag");
        instance.appendHeader("expect");
        instance.appendHeader("expires");
        instance.appendHeader("from");
        instance.appendHeader("host");
        instance.appendHeader("if-match");
        instance.appendHeader("if-modified-since");
        instance.appendHeader("if-none-match");
        instance.appendHeader("if-range");
        instance.appendHeader("if-unmodified-since");
        instance.appendHeader("last-modified");
        instance.appendHeader("link");
        instance.appendHeader("location");
        instance.appendHeader("max-forwards");
        instance.appendHeader("proxy-authenticate");
        instance.appendHeader("proxy-authorization");
        instance.appendHeader("range");
        instance.appendHeader("referer");
        instance.appendHeader("refresh");
        instance.appendHeader("retry-after");
        instance.appendHeader("server");
        instance.appendHeader("set-cookie");
        instance.appendHeader("strict-transport-security");
        instance.appendHeader("transfer-encoding");
        instance.appendHeader("user-agent");
        instance.appendHeader("vary");
        instance.appendHeader("via");
        instance.appendHeader("www-authenticate"); // index == 61
    }

    @Override
    public int getBaseIndex(){
        return baseIndex;
    }

    private void appendHeader(String name, String value){
        Http2TableEntry entry = new Http2TableEntry(name, value);
        array.add(entry);
    }
    private void appendHeader(String name){
        appendHeader(name, "");
    }

    /**
     * eg. with a value of 2, it should give you ":method GET Http2TableEntry"
     * @param index expecting a index >= baseIndex
     * @return null if not found
     */
    @Override
    public Http2TableEntry getEntry(int index) {
        if(index < baseIndex || index  >= array.size()+baseIndex)
            return null;
        return array.get(index - baseIndex);
    }

    @Override
    public Http2TableEntry getFirstEntry(String name) {
        Optional<Http2TableEntry> res = array.stream().filter(entry -> entry.getName().equals(name)).findFirst();
        return res.isPresent()? res.get() : null;
    }

    @Override
    public Http2TableEntry getFirstEntry(String name, String value){
        Optional<Http2TableEntry> res = array.stream().filter(entry -> entry.getName().equals(name) && entry.getValue().equals(value)).findFirst();
        return res.isPresent()? res.get() : null;
    }

    /**
     * @return -1 if not found
     */
    @Override
    public int getFirstEntryIndex(String name) {
        for(int i = 0; i < array.size(); i++){
            if(array.get(i).getName().equals(name)){
                return i + baseIndex;
            }
        }
        return -1;
    }

    /**
     * @return -1 if not found
     */
    @Override
    public int getFirstEntryIndex(String name, String value) {
        for(int i = 0; i < array.size(); i++){
            if(array.get(i).getName().equals(name) && array.get(i).getValue().equals(value)){
                return i + baseIndex;
            }
        }
        return -1;
    }

    public int size(){
        return array.size();
    }
}
 