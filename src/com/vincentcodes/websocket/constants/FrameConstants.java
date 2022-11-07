package com.vincentcodes.websocket.constants;

/**
 * Constans used for WebSocket Base Frames
 */
public class FrameConstants {
    /**
     * A status code is used on a close frame (opcode 0x8)
     * @see https://tools.ietf.org/html/rfc6455#section-7.4.1
     */
    public static class StatusCodes{
        public static short NONE = -1;
        /**
         * the purpose for which the connection was
         * established has been fulfilled
         */
        public static short NORMAL_CLOSURE = 1000;

        /**
         * server is going down or a browser having 
         * navigated away from a page
         */
        public static short GOING_AWAY = 1001;

        public static short PROTOCOL_ERROR = 1002;

        /**
         * received a data type that is unacceptable
         */
        public static short UNACCEPTABLE = 1003;

        /**
         * eg. non-UTF-8
         */
        public static short DATA_TYPE_INCONSISTENCY = 1007;

        public static short MESSAGE_TOO_LARGE = 1009;

        // 1010 is for client

        public static short ERROR_UNKNOWN = 1011;
    }
}
