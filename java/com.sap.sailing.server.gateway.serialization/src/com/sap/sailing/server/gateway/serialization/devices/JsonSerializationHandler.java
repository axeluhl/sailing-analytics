package com.sap.sailing.server.gateway.serialization.devices;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;

public interface JsonSerializationHandler<T> {
    JSONObject serialize(T object) throws IllegalArgumentException;
    T deserialize(JSONObject json) throws JsonDeserializationException;
}
