package com.vincentcodes.webserver.http2;

import java.util.ArrayList;
import java.util.Optional;

public class DynamicTable implements Http2Table{
    private ArrayList<Http2TableEntry> queue;
    private Http2Configuration config;
    private int baseIndex;

    public DynamicTable(Http2Configuration config, int baseIndex){
        queue = new ArrayList<>();
        this.baseIndex = baseIndex;
        this.config = config;
    }

    @Override
    public int getBaseIndex(){
        return baseIndex;
    }

    public void addHeader(Http2TableEntry entry) {
        queue.add(0, entry);
        trimToSize();
    }
    
    /**
     * Add an entry into the table (queue)
     * @return the entry which is being added into the table 
     * (if this is a static table, changes will not be made 
     * and returns <code>null</code>)
     */
    public Http2TableEntry addHeader(String name, String value){
        Http2TableEntry entry = new Http2TableEntry(name, value);
        addHeader(entry);
        return entry;
    }

    /**
     * eg. with a value of 2, it should give you ":method GET Http2TableEntry"
     * @param index expecting an index >= baseIndex
     * @return null if not found
     */
    @Override
    public Http2TableEntry getEntry(int index) {
        if(index < baseIndex || index  >= queue.size() + baseIndex)
            return null;
        return queue.get(index - baseIndex);
    }

    @Override
    public Http2TableEntry getFirstEntry(String name) {
        Optional<Http2TableEntry> res = queue.stream().filter(entry -> entry.getName().equals(name)).findFirst();
        return res.isPresent()? res.get() : null;
    }

    @Override
    public Http2TableEntry getFirstEntry(String name, String value){
        Optional<Http2TableEntry> res = queue.stream().filter(entry -> entry.getName().equals(name) && entry.getValue().equals(value)).findFirst();
        return res.isPresent()? res.get() : null;
    }

    /**
     * @return -1 if not found
     */
    @Override
    public int getFirstEntryIndex(String name) {
        for(int i = 0; i < queue.size(); i++){
            if(queue.get(i).getName().equals(name)){
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
        for(int i = 0; i < queue.size(); i++){
            if(queue.get(i).getName().equals(name) && queue.get(i).getValue().equals(value)){
                return i + baseIndex;
            }
        }
        return -1;
    }

    public Http2Configuration getConfig(){
        return config;
    }

    /**
     * @see https://tools.ietf.org/html/rfc7541#section-4.1
     */
    public int sizeInBytes(){
        return queue.stream().mapToInt(entry -> entry.size()).sum() + 32;
    }

    /**
     * Get number of entries
     */
    public int size(){
        return queue.size();
    }

    public void trimToSize(){
        trimToSize(config.getHeaderTableSize());
    }

    /**
     * This can cause entries to be evicted
     * @param maxSize the new max size of the table
     */
    public void trimToSize(int maxSize){
        int original = sizeInBytes();
        int decrease = 0;
        while(original - decrease > (maxSize + 32)){
            decrease += queue.remove(queue.size()-1).size();
        }
    }
}
