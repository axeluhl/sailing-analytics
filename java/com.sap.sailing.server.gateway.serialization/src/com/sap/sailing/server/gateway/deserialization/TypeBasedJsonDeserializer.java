package com.sap.sailing.server.gateway.deserialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.devices.TypeBasedServiceFinder;
import com.sap.sailing.server.gateway.serialization.devices.JsonSerializationHandler;

public abstract class TypeBasedJsonDeserializer<T> implements JsonDeserializer<T> {
    public static final String FIELD_TYPE = "type";

    protected abstract String getType();

    protected abstract T deserializeAfterCheckingType(JSONObject object) throws JsonDeserializationException;

    @Override
    public T deserialize(JSONObject object) throws JsonDeserializationException {
        if (!getType().equals(object.get(FIELD_TYPE))) {
            throw new JsonDeserializationException(
            		"Wrong type found (expected: " + getType() + ", but got: " + object.get(FIELD_TYPE) + ")");
        }
        return deserializeAfterCheckingType(object);
    }

    public static <T> T deserialize(TypeBasedServiceFinder<? extends JsonSerializationHandler<T>> serviceFinder, JSONObject object)
            throws JsonDeserializationException {
        return serviceFinder.findService((String) object.get(FIELD_TYPE)).deserialize(object);
    }
}
