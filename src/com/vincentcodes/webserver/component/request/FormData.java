package com.vincentcodes.webserver.component.request;

import com.vincentcodes.webserver.component.body.HttpBody;
import com.vincentcodes.webserver.component.header.HttpHeaders;

public class FormData {
    private String name;
    private String filename;
    private HttpHeaders headers;
    private HttpBody body;
    /**
     * Everything comes from "Content-Disposition"
     */
    public FormData(String name, String filename, HttpHeaders headers, HttpBody body){
        this.name = name;
        this.filename = filename;
        this.headers = headers;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public HttpHeaders getHeaders(){
        return headers;
    }

    public HttpBody getBody(){
        return body;
    }
}
