package com.sap.sailing.server.gateway.deserialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;

/**
 * Exception thrown on deserialization error from JSON data.
 */
public class JsonDeserializationException extends TransformationException {
    private static final long serialVersionUID = -6725762788023063937L;

    public JsonDeserializationException() {
        super(JSONObject.class, null, null);
    }

    public JsonDeserializationException(String message, Throwable innerException) {
        super(message, innerException);
    }

    public JsonDeserializationException(String message) {
        super(message);
    }

    public JsonDeserializationException(Throwable innerException) {
        super(innerException);
    }

}
