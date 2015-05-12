package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Duration;

public class DurationJsonSerializer implements JsonSerializer<Duration> {

    public static final String MILLIS = "millis";

    @Override
    public JSONObject serialize(Duration object) {
        JSONObject result = new JSONObject();
        result.put(MILLIS, object.asMillis());
        return result;
    }

}
