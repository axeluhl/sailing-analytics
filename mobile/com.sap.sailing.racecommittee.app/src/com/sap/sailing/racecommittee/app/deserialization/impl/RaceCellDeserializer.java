package com.sap.sailing.racecommittee.app.deserialization.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.base.racegroup.impl.RaceCellImpl;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.RaceCellJsonSerializer;

public class RaceCellDeserializer implements JsonDeserializer<RaceCell> {

    private JsonDeserializer<RaceLog> logDeserializer;
    private JsonDeserializer<Competitor> competitorDeserializer;

    public RaceCellDeserializer(JsonDeserializer<RaceLog> logDeserializer, JsonDeserializer<Competitor> competitorDeserializer) {
        this.logDeserializer = logDeserializer;
        this.competitorDeserializer = competitorDeserializer;
    }

    public RaceCell deserialize(JSONObject object)
            throws JsonDeserializationException {
        String name = object.get(RaceCellJsonSerializer.FIELD_NAME).toString();

        JSONObject logJson = Helpers.getNestedObjectSafe(object, RaceCellJsonSerializer.FIELD_RACE_LOG);
        RaceLog log = logDeserializer.deserialize(logJson);

        List<Competitor> competitors = deserializeCompetitorList(object);

        return new RaceCellImpl(name, log, competitors);
    }

    private List<Competitor> deserializeCompetitorList(JSONObject object) throws JsonDeserializationException {
        List<Competitor> competitors = new ArrayList<Competitor>();
        JSONArray competitorsJson = Helpers.getNestedArraySafe(object, RaceCellJsonSerializer.FIELD_COMPETITORS);
        for (Object competitorObject : competitorsJson) {
            JSONObject jsonObject = (JSONObject) competitorObject;
            Competitor competitor = competitorDeserializer.deserialize(jsonObject);
            competitors.add(competitor);
        }
        return competitors;
    }

}
