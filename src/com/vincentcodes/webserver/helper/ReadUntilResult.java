package com.vincentcodes.webserver.helper;

public class ReadUntilResult {
    private byte[] data;
    private boolean matchingStrFound;

    public ReadUntilResult(byte[] data, boolean matchingStrFound){
        this.data = data;
        this.matchingStrFound = matchingStrFound;
    }

    public byte[] data(){
        return data;
    }

    public boolean isMatchingStrFound(){
        return matchingStrFound;
    }
}
