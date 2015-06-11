package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.DurationJsonSerializer;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public class DurationJsonDeserializer implements JsonDeserializer<Duration> {

    @Override
    public Duration deserialize(JSONObject object) throws JsonDeserializationException {
        long millis = (long) object.get(DurationJsonSerializer.MILLIS);
        return new MillisecondsDurationImpl(millis);
    }

}
