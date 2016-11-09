package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONArray;

/**
 * Interface for serializer collections to Json.
 *
 * @param <T> Class to serialize.
 */
public interface JsonArraySerializer<T>
{
    /**
     * Serializes given object to a Json array.
     * 
     * @param object to be serialized.
     * @return serialized Json array.
     */
    JSONArray serialize(T object);
}
