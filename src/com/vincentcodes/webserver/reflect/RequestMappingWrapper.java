package com.vincentcodes.webserver.reflect;

import com.vincentcodes.webserver.annotaion.request.RequestMapping;

// I can't think of a good name, help.
public class RequestMappingWrapper extends AnnotationWrapper<RequestMapping>{
    public RequestMappingWrapper(RequestMapping annotation) {
        super(annotation);
    }

    public String combineWith(RequestMappingWrapper anotherMapping){
        String thisPattern = getRegexPattern();
        String thatPattern = anotherMapping.getRegexPattern();
        // join without trailing '$' and leading '^'
        String result = thisPattern.substring(0, thisPattern.length()-1) + thatPattern.substring(1);
        return result.replaceAll("//", "/"); // sanitize
    }

    public String value(){
        return annotation.value();
    }

    public boolean exact(){
        return annotation.exact();
    }

    public String getRegexPattern(){
        String annotationValue = value();
        String pattern;
        
        // String.format("^%s$");
        if(annotation.raw()){
            pattern = "^" + annotationValue + "$";
            return pattern;
        }

        if (annotationValue.contains("**")) {
            pattern = "^" + annotationValue.replace("**", "([^\\n\\r]*)");
        } else {
            pattern = "^" + annotationValue.replace("*", "([^\\n\\r/]*)");
        }

        int periodPos = pattern.lastIndexOf('.');
        int forwardSlashPos = pattern.lastIndexOf('/');
        // if /asd/ then /asd(/)?
        if(forwardSlashPos == pattern.length()-1){
            return pattern.substring(0, pattern.length()-1) + "(/)?$";
        }
        // if /asd/asd.txt or * then no modifications
        if(periodPos != -1 && forwardSlashPos < periodPos || pattern.endsWith("*")){
            return pattern + "$";
        }
        // if /asd then /asd(/)?
        return pattern + "(/)?$";
    }
}
