package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.impl.DurationJsonSerializer;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.shared.json.JsonDeserializationException;
import com.sap.sse.shared.json.JsonDeserializer;

public class DurationJsonDeserializer implements JsonDeserializer<Duration> {

    @Override
    public Duration deserialize(JSONObject object) throws JsonDeserializationException {
        long millis = (long) object.get(DurationJsonSerializer.MILLIS);
        return new MillisecondsDurationImpl(millis);
    }

}
