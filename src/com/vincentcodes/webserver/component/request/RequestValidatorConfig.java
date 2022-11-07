package com.vincentcodes.webserver.component.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A configuration used on {@link HttpRequestValidator}.
 */
public class RequestValidatorConfig {
    private List<String> mandatoryHeaders;
    private Optional<Pattern> bodyPattern;

    public RequestValidatorConfig(){
        this(new ArrayList<>(), null);
    }

    public RequestValidatorConfig(List<String> mandatoryHeaders){
        this(mandatoryHeaders, null);
    }

    public RequestValidatorConfig(List<String> mandatoryHeaders, Pattern bodyPattern){
        this.mandatoryHeaders = mandatoryHeaders;
        this.bodyPattern = Optional.ofNullable(bodyPattern);
    }
    
    public void addMandatoryHeader(String header){
        this.mandatoryHeaders.add(header);
    }

    public void addMandatoryHeaders(List<String> headers){
        this.mandatoryHeaders.addAll(headers);
    }

    public void setRequestBodyPattern(Pattern bodyPattern){
        this.bodyPattern = Optional.ofNullable(bodyPattern);
    }

    public List<String> getMandatoryHeaders() {
        return mandatoryHeaders;
    }

    public Optional<Pattern> getBodyPattern() {
        return bodyPattern;
    }
    
}
