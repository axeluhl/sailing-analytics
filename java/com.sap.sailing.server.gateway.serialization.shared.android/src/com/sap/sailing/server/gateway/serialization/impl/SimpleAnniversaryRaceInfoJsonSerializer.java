package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Date;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.anniversary.SimpleAnniversaryRaceInfo;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SimpleAnniversaryRaceInfoJsonSerializer implements JsonSerializer<SimpleAnniversaryRaceInfo>,JsonDeserializer<SimpleAnniversaryRaceInfo> {

    public static final String FIELD_RACE_NAME = "raceName";
    public static final String FIELD_REGATTA_NAME = "regattaName";
    public static final String FIELD_START_OF_RACE = "startOfRace";

    @Override
    public JSONObject serialize(SimpleAnniversaryRaceInfo object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_RACE_NAME, object.getIdentifier().getRaceName());
        result.put(FIELD_REGATTA_NAME, object.getIdentifier().getRegattaName());
        result.put(FIELD_START_OF_RACE, object.getStartOfRace().getTime());
        return result;
    }

    @Override
    public SimpleAnniversaryRaceInfo deserialize(JSONObject object) throws JsonDeserializationException {
        String raceName = object.get(SimpleAnniversaryRaceInfoJsonSerializer.FIELD_RACE_NAME).toString();
        String regattaName = object.get(SimpleAnniversaryRaceInfoJsonSerializer.FIELD_REGATTA_NAME).toString();
        Date startOfRace = new Date(((Number)object.get(SimpleAnniversaryRaceInfoJsonSerializer.FIELD_START_OF_RACE)).longValue());
        return new SimpleAnniversaryRaceInfo(new RegattaNameAndRaceName(regattaName, raceName),  startOfRace);
    }
}
