package com.sap.sailing.server.gateway.deserialization;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationHandler;
import com.sap.sse.common.TypeBasedServiceFinder;

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

    public static <T> T deserialize(TypeBasedServiceFinder<? extends TransformationHandler<T, JSONObject>> serviceFinder, JSONObject object)
            throws TransformationException {
        return serviceFinder.findService((String) object.get(FIELD_TYPE)).transformBack(object);
    }
}
