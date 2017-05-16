package com.sap.sailing.server.gateway.deserialization;

import org.json.simple.JSONObject;

/**
 * Interface for classes to deserialize {@link JSONObject}s.
 */
public interface JsonDeserializer<T> {
    T deserialize(JSONObject object) throws JsonDeserializationException;
}
