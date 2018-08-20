package com.sap.sailing.server.gateway.serialization.racelog.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.TimePoint;

public abstract class BaseRaceLogEventSerializer implements JsonSerializer<RaceLogEvent> {
    public static final String FIELD_CLASS = "@class";
    public static final String FIELD_ID = "id";
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_PASS_ID = "passId";
    public static final String FIELD_COMPETITORS = "competitors";
    public static final String FIELD_AUTHOR_NAME = "authorName";
    public static final String FIELD_AUTHOR_PRIORITY = "authorPriority";

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
        result.put(FIELD_CREATED_AT, object.getCreatedAt().asMillis());
        final TimePoint logicalTimePoint = object.getLogicalTimePoint();
        result.put(FIELD_TIMESTAMP, logicalTimePoint==null?null:logicalTimePoint.asMillis());
        result.put(FIELD_PASS_ID, object.getPassId());

        JSONArray competitors = new JSONArray();
        for (Competitor competitor : object.getInvolvedCompetitors()) {
            if (competitor != null) {
                competitors.add(competitorSerializer.serialize(competitor));
            }
        }
        result.put(FIELD_COMPETITORS, competitors);
        if (object.getAuthor() != null) {
            result.put(FIELD_AUTHOR_NAME, object.getAuthor().getName());
            result.put(FIELD_AUTHOR_PRIORITY, object.getAuthor().getPriority());
        }

        return result;
    }

}
