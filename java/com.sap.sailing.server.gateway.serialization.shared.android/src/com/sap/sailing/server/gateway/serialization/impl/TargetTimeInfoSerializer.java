package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.TargetTimeInfo.LegTargetTimeInfo;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class TargetTimeInfoSerializer implements JsonSerializer<TargetTimeInfo> {
    
    public static final String LEGS = "legs";
    public static final String DURATION_MILLIS = "durationMillis";
    public static final String LEG_START_TIME_MILLIS = "legStartMillis";
    public static final String LEG_DURATION_MILLIS = "legDurationMillis";
    public static final String LEG_DISTANCE_METERS = "legDistanceMeters";
    public static final String LEG_BEARING_DEGREES = "legBearingDegrees";
    public static final String LEG_TRUE_WIND_ANGLE_TO_LEG_DEGREES = "legTrueWindAngleToLegDegrees";
    public static final String LEG_WIND = "legWind";
    public static final String LEG_TYPE = "legType";
    
    private final JsonSerializer<Wind> windSerializer;
    
    public TargetTimeInfoSerializer(JsonSerializer<Wind> windSerializer) {
        super();
        this.windSerializer = windSerializer;
    }

    @Override
    public JSONObject serialize(TargetTimeInfo object) {
        final JSONArray legsAsJson = new JSONArray();
        final JSONObject result = new JSONObject();
        // redundant, but possibly useful to clients other than the deserializer:
        result.put(DURATION_MILLIS, object.getExpectedDuration().asMillis());
        result.put(LEGS, legsAsJson);
        for (final LegTargetTimeInfo legInfo : object.getLegTargetTimes()) {
            final JSONObject legAsJson = new JSONObject();
            legAsJson.put(LEG_START_TIME_MILLIS, legInfo.getExpectedStartTimePoint().asMillis());
            legAsJson.put(LEG_DURATION_MILLIS, legInfo.getExpectedDuration().asMillis());
            legAsJson.put(LEG_DISTANCE_METERS, legInfo.getDistance().getMeters());
            legAsJson.put(LEG_BEARING_DEGREES, legInfo.getLegBearing().getDegrees());
            // redundant, but possibly useful to clients other than the deserializer:
            legAsJson.put(LEG_TRUE_WIND_ANGLE_TO_LEG_DEGREES, legInfo.getTrueWindAngleToLeg().getDegrees());
            legAsJson.put(LEG_TYPE, legInfo.getLegType().name());
            legAsJson.put(LEG_WIND, windSerializer.serialize(legInfo.getWind()));
            legsAsJson.add(legAsJson);
        }
        return result;
    }
}
