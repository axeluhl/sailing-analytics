package com.sap.sse.filestorage;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.Util.Pair;

public class InvalidPropertiesException extends Exception {
    private static final long serialVersionUID = -7328897153875728802L;
    private final Map<Property, String> perPropertyMessages = new HashMap<>();

    public InvalidPropertiesException(String message) {
        super(message);
    }

    public InvalidPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPropertiesException(String message,
            @SuppressWarnings("unchecked") Pair<Property, String>... perPropertyMessages) {
        this(message, null, perPropertyMessages);
    }

    public InvalidPropertiesException(String message, Throwable cause,
            @SuppressWarnings("unchecked") Pair<Property, String>... perPropertyMessages) {
        super(message, cause);
        for (Pair<Property, String> pair : perPropertyMessages) {
            this.perPropertyMessages.put(pair.getA(), pair.getB());
        }
    }

    @Override
    /**
     * Returns the overall error message (always exists).
     */
    public String getMessage() {
        return super.getMessage();
    }

    public Map<Property, String> getPerPropertyMessage() {
        return perPropertyMessages;
    }
}
