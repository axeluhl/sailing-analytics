package com.sap.sailing.server.gateway.deserialization;

import org.json.simple.JSONArray;

/**
 * Interface for classes to deserialize {@link JSONArray}s.
 */
public interface JsonArrayDeserializer<T> {
    T deserialize(JSONArray arrayJSON) throws JsonDeserializationException;
}
