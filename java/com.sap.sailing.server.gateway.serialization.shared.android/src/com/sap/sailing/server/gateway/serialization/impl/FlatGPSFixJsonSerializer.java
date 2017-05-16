package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class FlatGPSFixJsonSerializer implements JsonSerializer<GPSFix> {
    public static final String FIELD_LON_DEG = "longitude";
    public static final String FIELD_LAT_DEG = "latitude";
    public static final String FIELD_TIME_MILLIS = "timestamp";
    public static final String FIELD_ACCURACY = "accuracy";
    public static final double NOT_AVAILABLE_THROUGH_SERVER = -1;

    @Override
    public JSONObject serialize(GPSFix object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_LON_DEG, object.getPosition().getLngDeg());
        result.put(FIELD_LAT_DEG, object.getPosition().getLatDeg());
        result.put(FIELD_TIME_MILLIS, object.getTimePoint().asMillis());
        result.put(FIELD_ACCURACY, NOT_AVAILABLE_THROUGH_SERVER);
        return result;
    }
}
