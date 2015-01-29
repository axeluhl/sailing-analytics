package com.sap.sse.filestorage;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.Util.Pair;

public class InvalidPropertiesException extends Exception {
    private static final long serialVersionUID = -7328897153875728802L;
    private final Map<FileStorageServiceProperty, String> perPropertyMessages = new HashMap<>();

    public InvalidPropertiesException(String message) {
        super(message);
    }

    public InvalidPropertiesException(String message, Throwable cause) {
        super(message, cause);
    }

    @SafeVarargs
    public InvalidPropertiesException(String message,
            Pair<FileStorageServiceProperty, String>... perPropertyMessages) {
        this(message, null, perPropertyMessages);
    }

    @SafeVarargs
    public InvalidPropertiesException(String message, Throwable cause,
           Pair<FileStorageServiceProperty, String>... perPropertyMessages) {
        super(message, cause);
        for (Pair<FileStorageServiceProperty, String> pair : perPropertyMessages) {
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

    public Map<FileStorageServiceProperty, String> getPerPropertyMessage() {
        return perPropertyMessages;
    }
}
