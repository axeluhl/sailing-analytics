package com.sap.sailing.selenium.api.regatta;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class RegattaRaces extends JsonWrapper {

    private static final String ATTRIBUTE_REGATTA_NAME = "regatta";
    private static final String ATTRIBUTE_RACES = "races";
    public RegattaRaces(JSONObject json) {
        super(json);
    }

    public String getRegattaName() {
        return get(ATTRIBUTE_REGATTA_NAME);
    }

    public RaceNameWithId[] getRaces() {
        RaceNameWithId[] array = getArray(ATTRIBUTE_RACES, RaceNameWithId.class);
        return array;
    }
}