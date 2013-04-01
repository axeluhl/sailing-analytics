package com.sap.sailing.server.gateway.serialization.competitor.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CompetitorIdJsonSerializer implements JsonSerializer<Competitor> {
    public static final String FIELD_ID = "id";

    @Override
    public JSONObject serialize(Competitor object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, object.getId());
        return result;
    }

}
