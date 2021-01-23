package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sse.common.Duration;
import com.sap.sse.shared.json.JsonSerializer;

public class DurationJsonSerializer implements JsonSerializer<Duration> {

    public static final String MILLIS = "millis";

    @Override
    public JSONObject serialize(Duration object) {
        JSONObject result = new JSONObject();
        result.put(MILLIS, object.asMillis());
        return result;
    }

}
