package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Date;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class SimpleRaceInfoJsonSerializer implements JsonSerializer<SimpleRaceInfo>,JsonDeserializer<SimpleRaceInfo> {

    public static final String FIELD_RACE_NAME = "raceName";
    public static final String FIELD_REGATTA_NAME = "regattaName";
    public static final String FIELD_START_OF_RACE = "startOfRace";

    @Override
    public JSONObject serialize(SimpleRaceInfo object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_RACE_NAME, object.getIdentifier().getRaceName());
        result.put(FIELD_REGATTA_NAME, object.getIdentifier().getRegattaName());
        result.put(FIELD_START_OF_RACE, object.getStartOfRace().getTime());
        return result;
    }

    @Override
    public SimpleRaceInfo deserialize(JSONObject object) throws JsonDeserializationException {
        String raceName = object.get(SimpleRaceInfoJsonSerializer.FIELD_RACE_NAME).toString();
        String regattaName = object.get(SimpleRaceInfoJsonSerializer.FIELD_REGATTA_NAME).toString();
        Date startOfRace = new Date(((Number)object.get(SimpleRaceInfoJsonSerializer.FIELD_START_OF_RACE)).longValue());
        return new SimpleRaceInfo(new RegattaNameAndRaceName(regattaName, raceName),  startOfRace);
    }
}
