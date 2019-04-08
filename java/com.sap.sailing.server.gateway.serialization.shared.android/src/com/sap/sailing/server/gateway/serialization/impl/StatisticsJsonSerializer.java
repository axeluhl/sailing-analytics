package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;

public class StatisticsJsonSerializer implements JsonSerializer<Statistics> {

    public static final String FIELD_NUMBER_OF_COMPETITORS = "numberOfCompetitors";
    public static final String FIELD_NUMBER_OF_REGATTAS = "numberOfRegattas";
    public static final String FIELD_NUMBER_OF_RACES = "numberOfRaces";
    public static final String FIELD_NUMBER_OF_TRACKED_RACES = "numberOfTrackedRaces";
    public static final String FIELD_NUMBER_OF_GPS_FIXES = "numberOfGPSFixes";
    public static final String FIELD_NUMBER_OF_WIND_FIXES = "numberOfWindFixes";
    public static final String FIELD_DISTANCE_TRAVELED_IN_METERS = "distanceTraveledInMeters";
    public static final String FIELD_MAX_SPEED = "maxSpeed";
    public static final String FIELD_FASTEST_COMPETITOR = "fastestCompetitor";
    public static final String FIELD_FASTEST_COMPETITOR_SPEED_IN_KNOTS = "fastestCompetitorSpeedInKnots";
    public static final String FIELD_TIMEPOINT_MILLIS = "timepointMillis";
    
    private final CompetitorJsonSerializer competitorJsonSerializer = CompetitorJsonSerializer.create();

    @Override
    public JSONObject serialize(Statistics object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NUMBER_OF_COMPETITORS, object.getNumberOfCompetitors());
        result.put(FIELD_NUMBER_OF_REGATTAS, object.getNumberOfRegattas());
        result.put(FIELD_NUMBER_OF_RACES, object.getNumberOfRaces());
        result.put(FIELD_NUMBER_OF_TRACKED_RACES, object.getNumberOfTrackedRaces());
        result.put(FIELD_NUMBER_OF_GPS_FIXES, object.getNumberOfGPSFixes());
        result.put(FIELD_NUMBER_OF_WIND_FIXES, object.getNumberOfWindFixes());
        result.put(FIELD_DISTANCE_TRAVELED_IN_METERS, object.getDistanceTraveled().getMeters());
        final Triple<Competitor, Speed, TimePoint> maxSpeed = object.getMaxSpeed();
        if (maxSpeed != null) {
            JSONObject maxSpeedObject = new JSONObject();
            
            maxSpeedObject.put(FIELD_FASTEST_COMPETITOR, competitorJsonSerializer.serialize(maxSpeed.getA()));
            maxSpeedObject.put(FIELD_FASTEST_COMPETITOR_SPEED_IN_KNOTS, maxSpeed.getB().getKnots());
            maxSpeedObject.put(FIELD_TIMEPOINT_MILLIS, maxSpeed.getC().asMillis());
            
            result.put(FIELD_MAX_SPEED, maxSpeedObject);
        }
        return result;
    }
}
