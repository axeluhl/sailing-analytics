package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceEntriesJsonSerializer implements JsonSerializer<RaceDefinition> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_COMPETITORS = "competitors";

    private final JsonSerializer<Competitor> competitorSerializer;

    public RaceEntriesJsonSerializer() {
        this(null);
    }

    public RaceEntriesJsonSerializer(JsonSerializer<Competitor> competitorSerializer) {
        this.competitorSerializer = competitorSerializer;
    }

    public JSONObject serialize(RaceDefinition race) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, race.getName());
        if(competitorSerializer != null) {
            JSONArray competitorsJson = new JSONArray();
            for (Competitor competitor: race.getCompetitors()) {
                competitorsJson.add(competitorSerializer.serialize(competitor));
            }
            result.put(FIELD_COMPETITORS, competitorsJson);
        }
        return result;
    }
}
