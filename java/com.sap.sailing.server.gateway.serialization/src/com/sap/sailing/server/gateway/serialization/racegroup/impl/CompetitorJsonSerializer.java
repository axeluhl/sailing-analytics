package com.sap.sailing.server.gateway.serialization.racegroup.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CompetitorJsonSerializer implements JsonSerializer<Competitor> {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";

    @Override
    public JSONObject serialize(Competitor object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, object.getId().toString());
        result.put(FIELD_NAME, object.getName());
        return result;
    }

}
