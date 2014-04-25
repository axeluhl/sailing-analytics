package com.sap.sailing.server.gateway.jaxrs.api;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class MarkPassingsJsonSerializer implements JsonSerializer<TrackedRace> {
    @Override
    public JSONObject serialize(TrackedRace object) {
        JSONObject result = new JSONObject();
        return result;
    }
}
