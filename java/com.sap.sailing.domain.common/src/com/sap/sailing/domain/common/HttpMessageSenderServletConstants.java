package com.sap.sailing.domain.common;

public interface HttpMessageSenderServletConstants {
    /**
     * The heartbeat message sent by the HTTP servlet for streamed message transmission
     */
    static final String PONG = "<pong>";
    
    static final long HEARTBEAT_TIME_IN_MILLISECONDS = 5000;
}
