package com.sap.sailing.domain.swisstimingadapter;

import java.io.IOException;

public class SwissTimingFormatException extends IOException {
    private static final long serialVersionUID = -1210353206288437237L;
    
    private final byte[] swissTimingMessage;

    public SwissTimingFormatException(String errorMessage, byte[] swissTimingMessage) {
        super(errorMessage);
        this.swissTimingMessage = swissTimingMessage;
    }

    public byte[] getSwissTimingMessage() {
        return swissTimingMessage;
    }
}
