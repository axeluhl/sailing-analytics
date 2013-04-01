package com.sap.sailing.server.gateway.serialization.competitor.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;

public class CompetitorJsonSerializer extends CompetitorIdJsonSerializer {
    public static final String FIELD_NAME = "name";

    @Override
    public JSONObject serialize(Competitor object) {
        JSONObject result = super.serialize(object);
        result.put(FIELD_NAME, object.getName());
        return result;
    }

}
