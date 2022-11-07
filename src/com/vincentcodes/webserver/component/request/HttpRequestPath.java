package com.vincentcodes.webserver.component.request;

import java.util.regex.Pattern;

import com.vincentcodes.webserver.reflect.RequestMappingWrapper;

public class HttpRequestPath {
    private String path;

    public HttpRequestPath(String path){
        this.path = path;
    }

    public boolean willMoveToParentDirectory(){
        return path.contains("..");
    }

    public boolean matchesPattern(String pattern){
        return Pattern.matches(pattern, path);
    }
    public boolean matchesPattern(RequestMappingWrapper requestMapping){
        if(requestMapping.exact())
            return path.equals(requestMapping.value());
        return matchesPattern(requestMapping.getRegexPattern());
    }

    public String get(){
        return path;
    }

    public String toString(){
        return path;
    }
}
