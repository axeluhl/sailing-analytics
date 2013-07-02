package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceColumnMasterDataJsonSerializer implements JsonSerializer<RaceColumn> {
    
    public static final String FIELD_MEDAL_RACE = "medalRace";
    public static final String FIELD_NAME = "name";
    
    @Override
    public JSONObject serialize(RaceColumn raceColumn) {
        JSONObject jsonRaceColumn = new JSONObject();
        jsonRaceColumn.put(FIELD_NAME, raceColumn.getName());
        jsonRaceColumn.put(FIELD_MEDAL_RACE, raceColumn.isMedalRace());
        return jsonRaceColumn;
    } 

}
