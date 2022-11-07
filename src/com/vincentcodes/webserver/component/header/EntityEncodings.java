package com.vincentcodes.webserver.component.header;

public enum EntityEncodings {
    GZIP     ("gzip"),
    COMPRESS ("compress"),
    DEFLATE  ("deflate"),
    BR       ("br"),
    IDENTITY ("identity"), // no compression
    ALL      ("*");        // any
    
    private String encodingType;

    private EntityEncodings(String encodingType){
        this.encodingType = encodingType;
    }

    public String value(){
        return encodingType;
    }

    public static EntityEncodings fromValue(String value){
        return valueOf(value.toLowerCase());
    }
}
