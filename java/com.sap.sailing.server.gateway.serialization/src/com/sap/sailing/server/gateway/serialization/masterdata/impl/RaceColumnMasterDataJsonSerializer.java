package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceColumnMasterDataJsonSerializer implements JsonSerializer<RaceColumn> {
    
    public static final String FIELD_RACE_LOG = "raceLog";
    public static final String FIELD_FLEETS = "fleets";
    public static final String FIELD_MEDAL_RACE = "medalRace";
    public static final String FIELD_NAME = "name";
    private final JsonSerializer<Fleet> fleetSerializer;
    private final JsonSerializer<RaceLog> raceLogSerializer;
    
    

    public RaceColumnMasterDataJsonSerializer(JsonSerializer<Fleet> fleetSerializer,
            JsonSerializer<RaceLog> raceLogSerializer) {
        this.fleetSerializer = fleetSerializer;
        this.raceLogSerializer = raceLogSerializer;
    }

    @Override
    public JSONObject serialize(RaceColumn raceColumn) {
        JSONObject jsonRaceColumn = new JSONObject();
        jsonRaceColumn.put(FIELD_NAME, raceColumn.getName());
        jsonRaceColumn.put(FIELD_MEDAL_RACE, raceColumn.isMedalRace());
        jsonRaceColumn.put(FIELD_FLEETS, createJsonArrayForFleets(raceColumn));
        return jsonRaceColumn;
    }

    private Object createJsonArrayForFleets(RaceColumn raceColumn) {
        JSONArray jsonFleets = new JSONArray();
        for (Fleet fleet : raceColumn.getFleets()) {
            JSONObject jsonFleet = fleetSerializer.serialize(fleet);
            addRaceLog(jsonFleet, raceColumn.getRaceLog(fleet));
            jsonFleets.add(jsonFleet);
        }
        return jsonFleets;
    }

    private void addRaceLog(JSONObject jsonFleet, RaceLog raceLog) {
        jsonFleet.put(FIELD_RACE_LOG, raceLogSerializer.serialize(raceLog));
    }

}
