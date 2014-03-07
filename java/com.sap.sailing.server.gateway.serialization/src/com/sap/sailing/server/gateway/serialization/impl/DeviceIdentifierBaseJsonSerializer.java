package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.server.gateway.deserialization.TypeBasedJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public abstract class DeviceIdentifierBaseJsonSerializer<T extends DeviceIdentifier> implements JsonSerializer<T> {
    public static final String FIELD_STRING_REPRESENTATION = "stringRepresentation";
    
    protected abstract JSONObject furtherSerialize(T object, JSONObject result);

    @Override
    public JSONObject serialize(T object) {
        JSONObject result = new JSONObject();
        result.put(TypeBasedJsonDeserializer.FIELD_TYPE, object.getIdentifierType());
        result.put(FIELD_STRING_REPRESENTATION, object.getStringRepresentation());
        return furtherSerialize(object, result);
    }

}
