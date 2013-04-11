package com.sap.sailing.server.gateway.serialization.racegroup.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceCellJsonSerializer implements JsonSerializer<RaceCell> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_RACE_LOG = "raceLog";
    public static final String FIELD_COMPETITORS = "competitors";

    private JsonSerializer<RaceLog> logSerializer;
    private JsonSerializer<Competitor> competitorSerializer;

    public RaceCellJsonSerializer(JsonSerializer<RaceLog> logSerializer, JsonSerializer<Competitor> competitorSerializer) {
        this.logSerializer = logSerializer;
        this.competitorSerializer = competitorSerializer;
    }

    @Override
    public JSONObject serialize(RaceCell object) {
        JSONObject result = new JSONObject();

        result.put(FIELD_NAME, object.getName());
        result.put(FIELD_RACE_LOG, logSerializer.serialize(object.getRaceLog()));
        JSONArray competitorArray = getJSONCompetitorArray(object);
        result.put(FIELD_COMPETITORS, competitorArray);

        return result;
    }

    private JSONArray getJSONCompetitorArray(RaceCell object) {
        JSONArray competitorArray = new JSONArray();
        for (Competitor competitor : object.getCompetitors()) {
            competitorArray.add(competitorSerializer.serialize(competitor));
        }
        return competitorArray;
    }

}
