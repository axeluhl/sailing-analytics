package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.DynamicBoat;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util.Pair;

public class RaceEntriesJsonSerializer implements JsonSerializer<RaceDefinition> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_COMPETITORS = "competitors";

    private final CompetitorAndBoatJsonSerializer competitorAndBoatSerializer;

    public RaceEntriesJsonSerializer() {
        this(null);
    }

    public RaceEntriesJsonSerializer(CompetitorAndBoatJsonSerializer competitorAndBoatSerializer) {
        this.competitorAndBoatSerializer = competitorAndBoatSerializer;
    }

    public JSONObject serialize(RaceDefinition race) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, race.getName());
        if(competitorAndBoatSerializer != null) {
            JSONArray competitorsJson = new JSONArray();
            for (Entry<Competitor, Boat> competitorAndBoatEntry: race.getCompetitorsAndTheirBoats().entrySet()) {
                competitorsJson.add(competitorAndBoatSerializer.serialize(new Pair<>(competitorAndBoatEntry.getKey(),
                        (DynamicBoat) competitorAndBoatEntry.getValue())));
            }
            result.put(FIELD_COMPETITORS, competitorsJson);
        }
        return result;
    }
}
