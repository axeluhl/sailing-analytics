package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class AnniversaryRaceInfoJsonSerializer implements JsonSerializer<AnniversaryRaceInfo> {

    public static final String FIELD_EVENT_ID = "eventID";
    public static final String FIELD_RACE_NAME = "raceName";
    public static final String FIELD_REGATTA_NAME = "regattaName";
    public static final String FIELD_LEADERBOARD_NAME = "leaderboardName";
    public static final String FIELD_START_OF_RACE = "startOfRace";

    @Override
    public JSONObject serialize(AnniversaryRaceInfo object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_EVENT_ID, object.getEventID());
        result.put(FIELD_RACE_NAME, object.getIdentifier().getRaceName());
        result.put(FIELD_REGATTA_NAME, object.getIdentifier().getRegattaName());
        result.put(FIELD_LEADERBOARD_NAME, object.getLeaderboardName());
        result.put(FIELD_START_OF_RACE, object.getStartOfRace());
        return result;
    }
}
