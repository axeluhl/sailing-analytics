package com.sap.sse.util.impl;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Logger;

public class UUIDHelper {
    private static final Logger logger = Logger.getLogger(UUIDHelper.class.getName());
    
    /**
     * If the {@link Object#toString() string representation} of {@code serializableId} {@link UUID#fromString(String) parses}
     * as a {@link UUID}, the resulting {@link UUID} is returned. Otherwise, the {@code serializableId} parameter's value
     * is returned unchanged.
     */
    public static Serializable tryUuidConversion(Serializable serializableId) {
        try {
            return UUID.fromString(serializableId.toString());
        } catch (IllegalArgumentException iae) {
            logger.fine("The serializable " + serializableId.toString() + " could not be converted to a UUID");
        }
        return serializableId;
    }
}
