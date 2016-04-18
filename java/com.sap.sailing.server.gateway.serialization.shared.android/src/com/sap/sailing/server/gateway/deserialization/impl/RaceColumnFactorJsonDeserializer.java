package com.sap.sailing.server.gateway.deserialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.impl.RaceColumnFactorImpl;
import com.sap.sailing.domain.common.impl.RaceColumnConstants;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class RaceColumnFactorJsonDeserializer implements JsonDeserializer<RaceColumnFactorImpl> {

    @Override
    public RaceColumnFactorImpl deserialize(JSONObject object) throws JsonDeserializationException {
        String leaderboard_name = (String) object.get(RaceColumnConstants.LEADERBOARD_NAME);
        String leaderboard_display_name = (String) object.get(RaceColumnConstants.LEADERBOARD_DISPLAY_NAME);
        RaceColumnFactorImpl raceColumnFactor = new RaceColumnFactorImpl(leaderboard_name, leaderboard_display_name);
        JSONArray race_columns = (JSONArray) object.get(RaceColumnConstants.RACE_COLUMNS);
        int size = race_columns.size();
        for (int i = 0; i < size; i++) {
            JSONObject data = (JSONObject) race_columns.get(i);
            String race_column_name = (String) data.get(RaceColumnConstants.RACE_COLUMN_NAME);
            Double explicit_factor = (Double) data.get(RaceColumnConstants.EXPLICIT_FACTOR);
            Double factor = (Double) data.get(RaceColumnConstants.FACTOR);
            raceColumnFactor.addRaceColumn(race_column_name, explicit_factor, factor);
        }
        return raceColumnFactor;
    }
}
