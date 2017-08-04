package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Date;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.anniversary.AnniversaryRaceInfo;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class AnniversaryRaceInfoJsonSerializer implements JsonSerializer<AnniversaryRaceInfo>, JsonDeserializer<AnniversaryRaceInfo> {

    public static final String FIELD_EVENT_ID = "eventID";
    public static final String FIELD_LEADERBOARD_NAME = "leaderboardName";

    @Override
    public JSONObject serialize(AnniversaryRaceInfo object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_EVENT_ID, object.getEventID().toString());
        result.put(SimpleAnniversaryRaceInfoJsonSerializer.FIELD_RACE_NAME, object.getIdentifier().getRaceName());
        result.put(SimpleAnniversaryRaceInfoJsonSerializer.FIELD_REGATTA_NAME, object.getIdentifier().getRegattaName());
        result.put(FIELD_LEADERBOARD_NAME, object.getLeaderboardName());
        result.put(SimpleAnniversaryRaceInfoJsonSerializer.FIELD_START_OF_RACE, object.getStartOfRace());
        return result;
    }
    
    @Override
    public AnniversaryRaceInfo deserialize(JSONObject object) throws JsonDeserializationException {
        String eventId = object.get(FIELD_EVENT_ID).toString();
        String raceName = object.get(SimpleAnniversaryRaceInfoJsonSerializer.FIELD_RACE_NAME).toString();
        String regattaName = object.get(SimpleAnniversaryRaceInfoJsonSerializer.FIELD_REGATTA_NAME).toString();
        String leaderboardName = object.get(FIELD_LEADERBOARD_NAME).toString();
        Date startOfRace = (Date) object.get(SimpleAnniversaryRaceInfoJsonSerializer.FIELD_START_OF_RACE);
        return new AnniversaryRaceInfo(new RegattaNameAndRaceName(regattaName, raceName), leaderboardName, startOfRace, UUID.fromString(eventId));
    }
}
