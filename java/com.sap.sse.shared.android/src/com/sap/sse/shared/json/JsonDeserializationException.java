package com.sap.sse.shared.json;

import com.sap.sse.common.TransformationException;

/**
 * Exception thrown on deserialization error from JSON data.
 */
public class JsonDeserializationException extends TransformationException {
    private static final long serialVersionUID = -6725762788023063937L;

    public JsonDeserializationException() {
        super();
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
