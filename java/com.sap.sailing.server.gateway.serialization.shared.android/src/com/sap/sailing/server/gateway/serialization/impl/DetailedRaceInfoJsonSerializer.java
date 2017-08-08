package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Date;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class DetailedRaceInfoJsonSerializer implements JsonSerializer<DetailedRaceInfo>, JsonDeserializer<DetailedRaceInfo> {

    public static final String FIELD_EVENT_ID = "eventID";
    public static final String FIELD_LEADERBOARD_NAME = "leaderboardName";
    private static final String FIELD_REMOTEURL = "remoteUrl";

    @Override
    public JSONObject serialize(DetailedRaceInfo object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_EVENT_ID, object.getEventID().toString());
        result.put(SimpleRaceInfoJsonSerializer.FIELD_RACE_NAME, object.getIdentifier().getRaceName());
        result.put(SimpleRaceInfoJsonSerializer.FIELD_REGATTA_NAME, object.getIdentifier().getRegattaName());
        result.put(FIELD_LEADERBOARD_NAME, object.getLeaderboardName());
        result.put(SimpleRaceInfoJsonSerializer.FIELD_START_OF_RACE, object.getStartOfRace());
        result.put(FIELD_REMOTEURL, object.getRemoteUrl());
        return result;
    }
    
    @Override
    public DetailedRaceInfo deserialize(JSONObject object) throws JsonDeserializationException {
        String eventId = object.get(FIELD_EVENT_ID).toString();
        String raceName = object.get(SimpleRaceInfoJsonSerializer.FIELD_RACE_NAME).toString();
        String regattaName = object.get(SimpleRaceInfoJsonSerializer.FIELD_REGATTA_NAME).toString();
        String leaderboardName = object.get(FIELD_LEADERBOARD_NAME).toString();
        Date startOfRace = (Date) object.get(SimpleRaceInfoJsonSerializer.FIELD_START_OF_RACE);
        String remoteUrl = (String) object.get(FIELD_REMOTEURL);
        return new DetailedRaceInfo(new RegattaNameAndRaceName(regattaName, raceName), leaderboardName, startOfRace, UUID.fromString(eventId),remoteUrl);
    }
}