package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public abstract class BaseRaceLogEventSerializer implements JsonSerializer<RaceLogEvent> {
    public static final String FIELD_CLASS = "@class";
    public static final String FIELD_ID = "id";
    public static final Object FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_PASS_ID = "passId";
    public static final String FIELD_COMPETITORS = "competitors";

    protected abstract String getClassFieldValue();

    protected JsonSerializer<Competitor> competitorSerializer;

    public BaseRaceLogEventSerializer(JsonSerializer<Competitor> competitorSerializer) {
        this.competitorSerializer = competitorSerializer;
    }

    @Override
    public JSONObject serialize(RaceLogEvent object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_CLASS, getClassFieldValue());
        result.put(FIELD_ID, object.getId().toString());
        result.put(FIELD_TIMESTAMP, object.getTimePoint().asMillis());
        result.put(FIELD_PASS_ID, object.getPassId());

        JSONArray competitors = new JSONArray();
        for (Competitor competitor : object.getInvolvedBoats()) {
            competitors.add(competitorSerializer.serialize(competitor));
        }
        result.put(FIELD_COMPETITORS, competitors);

        return result;
    }

}
