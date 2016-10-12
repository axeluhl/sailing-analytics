package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class FlatGPSFixJsonSerializer implements JsonSerializer<GPSFix> {
    public static final String FIELD_LON_DEG = "longitude";
    public static final String FIELD_LAT_DEG = "latitude";
    public static final String FIELD_TIME_MILLIS = "timestamp";
    public static final String FIELD_ACCURACY = "accuracy";

    @Override
    public JSONObject serialize(GPSFix object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_LON_DEG, object.getPosition().getLngDeg());
        result.put(FIELD_LAT_DEG, object.getPosition().getLatDeg());
        result.put(FIELD_TIME_MILLIS, object.getTimePoint().asMillis());
        // TODO Insert Accuracy if someday available from the server
        // Buoy Pinger app will interpret no accuracy in JSON message as MarkPingInfo.NOT_SET_BY_USER (-1) and store that in DB
        // For the user these values are displayed as R.string.unknown ("n/a");
        // (CheckinManager in Buoy Pinger can accept messages with and without accuracy)
        
        // result.put(FIELD_ACCURACY, XXXXXXXXX);
        return result;
    }
}
