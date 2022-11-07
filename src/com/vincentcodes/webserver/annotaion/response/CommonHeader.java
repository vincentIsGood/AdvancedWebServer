package com.vincentcodes.webserver.annotaion.response;

/**
 * Used to indicate the handlers inside a class will
 * add a common header to respond to the client. It
 * is especially useful when you want to deal with 
 * "Access-Control-Allow-Origin" because you do not
 * need to add that header to each of your handler.
 */
public @interface CommonHeader {
    
}
