package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class StatisticsJsonSerializer implements JsonSerializer<Statistics> {

    public static final String FIELD_NUMBER_OF_COMPETITORS = "numberOfCompetitors";
    public static final String FIELD_NUMBER_OF_REGATTAS = "numberOfRegattas";
    public static final String FIELD_NUMBER_OF_RACES = "numberOfRaces";
    public static final String FIELD_NUMBER_OF_TRACKED_RACES = "numberOfTrackedRaces";
    public static final String FIELD_NUMBER_OF_GPS_FIXES = "numberOfGPSFixes";
    public static final String FIELD_NUMBER_OF_WIND_FIXES = "numberOfWindFixes";
    public static final String FIELD_DISTANCE_TRAVELED = "distanceTraveled";

    @Override
    public JSONObject serialize(Statistics object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NUMBER_OF_COMPETITORS, object.getNumberOfCompetitors());
        result.put(FIELD_NUMBER_OF_REGATTAS, object.getNumberOfRegattas());
        result.put(FIELD_NUMBER_OF_RACES, object.getNumberOfRaces());
        result.put(FIELD_NUMBER_OF_TRACKED_RACES, object.getNumberOfTrackedRaces());
        result.put(FIELD_NUMBER_OF_GPS_FIXES, object.getNumberOfGPSFixes());
        result.put(FIELD_NUMBER_OF_WIND_FIXES, object.getNumberOfWindFixes());
        result.put(FIELD_DISTANCE_TRAVELED, object.getDistanceTraveled().getMeters());
        return result;
    }
}
