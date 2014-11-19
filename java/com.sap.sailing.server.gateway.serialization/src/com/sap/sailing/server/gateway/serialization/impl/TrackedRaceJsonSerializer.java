package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.TimePoint;

public class TrackedRaceJsonSerializer implements JsonSerializer<TrackedRace> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_START_TIME = "startTime";

    @Override
    public JSONObject serialize(TrackedRace trackedRace) {
        JSONObject result = new JSONObject();
        result.put("name", trackedRace.getRace().getName());
        TimePoint startTime = trackedRace.getStartOfRace();
        result.put(FIELD_START_TIME, startTime == null ? Long.MAX_VALUE : startTime.asMillis());
        return result;
    }
}
