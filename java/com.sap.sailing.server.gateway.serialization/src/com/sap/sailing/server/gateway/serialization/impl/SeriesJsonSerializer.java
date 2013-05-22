package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SeriesJsonSerializer implements JsonSerializer<Series> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_FLEETS = "fleets";
    public static final String FIELD_RACES = "races";

    private final JsonSerializer<Fleet> fleetSerializer;

    public SeriesJsonSerializer(JsonSerializer<Fleet> fleetSerializer) {
        this.fleetSerializer = fleetSerializer;
    }

    public JSONObject serialize(Series series) {
        JSONObject result = new JSONObject();
        
        result.put(FIELD_NAME, series.getName());

        JSONArray fleetsJson = new JSONArray();
        for (Fleet fleet : series.getFleets()) {
            fleetsJson.add(fleetSerializer.serialize(fleet));
        }
        result.put(FIELD_FLEETS, fleetsJson);

        JSONArray racesJson = new JSONArray();
        for(RaceColumn raceColumn: series.getRaceColumns()) {
            racesJson.add(raceColumn.getName());
        }
        result.put(FIELD_RACES, racesJson);
        
        return result;
    }
}
