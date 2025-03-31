package com.sap.sailing.server.gateway.serialization.racelog.tracking.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.GPSFixJsonHandler;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;
import com.sap.sse.shared.json.JsonSerializer;

public class GPSFixJsonHandlerImpl<T extends GPSFix> implements GPSFixJsonHandler {
    private final JsonDeserializer<T> deserializer;
    private final JsonSerializer<T> serializer;

    public GPSFixJsonHandlerImpl(JsonDeserializer<T> deserializer, JsonSerializer<T> serializer) {
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject transformForth(GPSFix fix) throws IllegalArgumentException {
        return serializer.serialize((T) fix);
    }

    @Override
    public GPSFix transformBack(JSONObject json) throws JsonDeserializationException {
        return deserializer.deserialize(json);
    }
}
