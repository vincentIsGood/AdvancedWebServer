package com.vincentcodes.webserver.dispatcher;

public class HttpHandlerOptions {
    private boolean wholeFile;

    public boolean isWholeFile() {
        return wholeFile;
    }
    
    public static HttpHandlerOptions empty(){
        return new HttpHandlerOptions();
    }

    public static class Builder{
        private HttpHandlerOptions result;

        public Builder(){
            result = new HttpHandlerOptions();
        }

        public Builder wholeFile(){
            result.wholeFile = true;
            return this;
        }

        /**
         * Do not re-use the builder unless you want the same
         * options again.
         */
        public HttpHandlerOptions build(){
            return result;
        }
    }
}
