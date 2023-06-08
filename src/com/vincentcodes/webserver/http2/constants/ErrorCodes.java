package com.vincentcodes.webserver.http2.constants;

/**
 * @see https://tools.ietf.org/html/rfc7540#section-7
 */
public enum ErrorCodes {
    NO_ERROR            ((byte) 0x0), // graceful shutdown
    PROTOCOL_ERROR      ((byte) 0x1), // protocol error detected

    INTERNAL_ERROR      ((byte) 0x2), // implementation fault
    FLOW_CONTROL_ERROR  ((byte) 0x3), // flow-control limits exceeded

    SETTINGS_TIMEOUT    ((byte) 0x4), // settings not acknowledged

    STREAM_CLOSED       ((byte) 0x5), // frame received for closed stream

    FRAME_SIZE_ERROR    ((byte) 0x6), // frame size incorrect
    REFUSED_STREAM      ((byte) 0x7), // stream not processed
    CANCEL              ((byte) 0x8), // stream cancelled
    COMPRESSION_ERROR   ((byte) 0x9), // compression state not update

    CONNECT_ERROR       ((byte) 0xa), // TCP connection error for CONNECT method

    ENHANCE_YOUR_CLAM   ((byte) 0xb), // processing capacity exceeded

    INADEQUATE_SECURITY ((byte) 0xc), //negotiated TLS parameters not acceptable

    HTTP_1_1_REQUIRED   ((byte) 0xd); // use HTTP/1.1 for the request

    private static final ErrorCodes[] VALUES = values();

    public final byte value;

    private ErrorCodes(byte value) {
        this.value = value;
    }

    /**
     * @param b
     * @return null if not found
     */
    public static ErrorCodes fromByte(byte b){
        for(ErrorCodes ele : VALUES) if(ele.value == b) return ele;
        return null;
    }
}
