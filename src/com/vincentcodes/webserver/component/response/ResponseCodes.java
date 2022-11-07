package com.vincentcodes.webserver.component.response;

public final class ResponseCodes {
    /**
     * Selects a description for a reponse code.
     * Note that only common ones are allowed.
     * 
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
     */
    public static String getCodeDescription(int responseCode){
        switch(responseCode){
            // case 100: return "Continue"; // Will not implement Continue
            case 101: return "Switching Protocol";

            case 200: return "OK";
            case 201: return "Created";
            case 202: return "Accepted";
            case 204: return "No Content";
            case 205: return "Reset Content";
            case 206: return "Partial Content";

            case 301: return "Moved Permanently";
            case 302: return "Found";
            case 303: return "See Other";
            case 304: return "Not Modified";
            case 307: return "Temporary Redirect";
            case 308: return "Permanent Redirect";

            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 408: return "Request Timeout";
            case 409: return "Conflict";
            case 410: return "Gone";
            case 411: return "Length Required";
            case 415: return "Unsupported Media Type";
            case 416: return "Range Not Satisfiable";
            case 426: return "Upgrade Required";
            case 429: return "Too Many Requests";

            case 500: return "Internal Server Error";
            case 505: return "HTTP Version Not Supported";
        }
        return null;
    }

    public static boolean isResponseCodeSupported(int responseCode){
        return getCodeDescription(responseCode) != null;
    }

    public static boolean isErrorResponseCode(int responseCode){
        return responseCode >= 400;
    }
}
