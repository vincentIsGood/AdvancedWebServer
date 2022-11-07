package com.vincentcodes.webserver.component.request;

import com.vincentcodes.webserver.component.body.HttpBody;

public class FormData {
    private String name;
    private String filename;
    private HttpBody body;
    /**
     * Everything comes from "Content-Disposition"
     */
    public FormData(String name, String filename, HttpBody body){
        this.name = name;
        this.filename = filename;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public HttpBody getBody(){
        return body;
    }
}
