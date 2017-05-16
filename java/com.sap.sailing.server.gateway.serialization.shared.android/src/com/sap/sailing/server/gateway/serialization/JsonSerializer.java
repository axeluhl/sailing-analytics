package com.sap.sailing.server.gateway.serialization;

import org.json.simple.JSONObject;

/**
 * Interface for serializer to Json.
 *
 * @param <T> Object class to serialize.
 */
public interface JsonSerializer<T>
{
    /**
     * Serializes given object to a Json object.
     * 
     * @param object to be serialized.
     * @return serialized Json object.
     */
    JSONObject serialize(T object);
}
