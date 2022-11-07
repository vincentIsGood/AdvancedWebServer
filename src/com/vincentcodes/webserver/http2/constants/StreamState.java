package com.vincentcodes.webserver.http2.constants;

/**
 * @see https://tools.ietf.org/html/rfc7540#section-5.1
 */
public enum StreamState {
    /**
     * All streams start in idle state.
     * <p>
     * Can Transition To: RESERVED_LOCAL, RESERVED_REMOTE, OPEN
     */
    IDLE,

    /**
     * I have not seen "reserved" state used by anyone yet.
     * Hence, it is not supported for now.
     * <p>
     * Can Transition To: HALF_CLOSED_REMOTE, CLOSED
     */
    RESERVED_LOCAL,

    /**
     * I have not seen "reserved" state used by anyone yet.
     * Hence, it is not supported for now.
     * <p>
     * Can Transition To: HALF_CLOSED_LOCAL, CLOSED
     */
    RESERVED_REMOTE,

    /**
     * A stream in the "open" state may be used by both peers to send
     * frames of any type.
     * <p>
     * Can Transition To: HALF_CLOSED_LOCAL, HALF_CLOSED_REMOTE, CLOSED
     */
    OPEN,

    /**
     * A stream that is in the "half-closed (local)" state cannot be used
     * for sending frames other than WINDOW_UPDATE, PRIORITY, and
     * RST_STREAM.
     * <p>
     * Can Transition To: CLOSED
     */
    HALF_CLOSED_LOCAL,

    /**
     * A stream that is "half-closed (remote)" is no longer being used by
     * the peer to send frames.
     * <p>
     * Can Transition To: CLOSED
     */
    HALF_CLOSED_REMOTE,

    /**
     * The "closed" state is the terminal state.
     * <p>
     * A stream is closed after receiving / sending END_STREAM 
     * flagged frame or RST_STREAM frame.
     */
    CLOSED;
}
