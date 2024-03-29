package com.vincentcodes.webserver.dispatcher;

/**
 * @link HttpHandlerResult
 */
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

        /**
         * Allow the payload to be downloaded by the client as 
         * an attachment to prevent clients from processing the 
         * payload.
         */
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
