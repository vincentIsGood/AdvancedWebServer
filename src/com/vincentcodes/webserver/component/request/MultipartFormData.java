package com.vincentcodes.webserver.component.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.vincentcodes.webserver.component.body.HttpBody;
import com.vincentcodes.webserver.component.header.HttpHeaders;
import com.vincentcodes.webserver.util.HtmlEntityUtils;

/**
 * To identify each formData, the parameter "name" 
 * from "Content-Disposition" is used. If the name
 * repeats, the new one will <b>replace</b> the old one.
 */
public class MultipartFormData {
    private Map<String, FormData> formData;

    public MultipartFormData(){
        formData = new HashMap<>();
    }

    /**
     * @param headerValue metadata for the body 
     * eg. form-data; name="uploadfile"; filename="newfile.txt"
     * @param body the body after "Content-Disposition" 
     * (This should not be empty) or the data that comes after 
     * the headers.
     */
    public void put(HttpHeaders headers, HttpBody body){
        String headerValue = HtmlEntityUtils.unescapeDecimal(headers.getHeader("content-disposition"));
        String name = HttpHeaders.extractParameter(headerValue, "name");
        String filename = HttpHeaders.extractParameter(headerValue, "filename");
        put(name, filename, headers, body);
    }

    // public void put(String name, HttpBody body){
    //     put(name, null, body);
    // }

    /**
     * Creates a new FormData and stores it.
     * @param name should not be null. This param will be the key of the whole data.
     * @param filename nullable
     * @param body should not be null
     */
    public void put(String name, String filename, HttpHeaders headers, HttpBody body){
        formData.put(name, new FormData(name, filename, headers, body));
    }

    public FormData getData(String name){
        return formData.get(name);
    }

    public Set<String> getNames(){
        return formData.keySet();
    }
}
