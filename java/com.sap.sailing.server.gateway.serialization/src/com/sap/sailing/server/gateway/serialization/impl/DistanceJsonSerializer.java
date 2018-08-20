package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Distance;

public class DistanceJsonSerializer implements JsonSerializer<Distance> {
    
    public static final String FIELD_GEOGRAPHICAL_MILES = "geographicalMiles";
    public static final String FIELD_SEA_MILES = "seaMiles";
    public static final String FIELD_NAUTICAL_MILES = "nauticalMiles";
    public static final String FIELD_METERS = "meters";
    public static final String FIELD_KILOMETERS = "kilometers";
    public static final String FIELD_CENTRAL_ANGLE_DEG = "centralAngleDeg";
    public static final String FIELD_CENTRAL_ANGLE_RAD = "centralAngleRad";

    @Override
    public JSONObject serialize(Distance distance) {
        JSONObject jsonDistance = new JSONObject();
        jsonDistance.put(FIELD_GEOGRAPHICAL_MILES, distance.getGeographicalMiles());
        jsonDistance.put(FIELD_SEA_MILES, distance.getSeaMiles());
        jsonDistance.put(FIELD_NAUTICAL_MILES, distance.getNauticalMiles());
        jsonDistance.put(FIELD_METERS, distance.getMeters());
        jsonDistance.put(FIELD_KILOMETERS, distance.getKilometers());
        jsonDistance.put(FIELD_CENTRAL_ANGLE_DEG, distance.getCentralAngleDeg());
        jsonDistance.put(FIELD_CENTRAL_ANGLE_RAD, distance.getCentralAngleRad());
        return jsonDistance;
    }

}
