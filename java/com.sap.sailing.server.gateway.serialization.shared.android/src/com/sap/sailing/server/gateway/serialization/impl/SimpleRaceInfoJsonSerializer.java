package com.sap.sailing.server.gateway.serialization.impl;

import java.net.URL;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class SimpleRaceInfoJsonSerializer implements JsonSerializer<SimpleRaceInfo> {

    public static final String FIELD_RACE_NAME = "raceName";
    public static final String FIELD_REGATTA_NAME = "regattaName";
    public static final String FIELD_START_OF_RACE = "startOfRaceAsMillis";

    @Override
    public JSONObject serialize(SimpleRaceInfo object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_RACE_NAME, object.getIdentifier().getRaceName());
        result.put(FIELD_REGATTA_NAME, object.getIdentifier().getRegattaName());
        result.put(FIELD_START_OF_RACE, object.getStartOfRace().asMillis());
        return result;
    }

    public SimpleRaceInfo deserialize(JSONObject object, URL remoteUrl) throws JsonDeserializationException {
        String raceName = object.get(FIELD_RACE_NAME).toString();
        String regattaName = object.get(FIELD_REGATTA_NAME).toString();
        TimePoint startOfRace = new MillisecondsTimePoint(((Number) object.get(FIELD_START_OF_RACE)).longValue());
        return new SimpleRaceInfo(new RegattaNameAndRaceName(regattaName, raceName), startOfRace, remoteUrl);
    }
}
