package com.vincentcodes.webserver.dispatcher;

public class HttpHandlerOptions {
    private boolean isAttachment;

    public boolean isAttachment() {
        return isAttachment;
    }
    
    public static HttpHandlerOptions empty(){
        return new HttpHandlerOptions();
    }

    public static class Builder{
        private HttpHandlerOptions result;

        public Builder(){
            result = new HttpHandlerOptions();
        }

        public Builder asAttachment(){
            result.isAttachment = true;
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
