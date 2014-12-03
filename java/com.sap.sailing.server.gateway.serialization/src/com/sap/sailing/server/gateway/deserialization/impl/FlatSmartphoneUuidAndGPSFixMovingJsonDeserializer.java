package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.MeterPerSecondSpeedImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.Util.Pair;

/**
 * Make serialization on the smartphone easier by providing a flat structure rather than nested JSON documents.
 * 
 * @author Fredrik Teschke
 *
 */
public class FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer implements
        JsonDeserializer<Pair<UUID, List<GPSFixMoving>>> {
    public static final String DEVICE_UUID = "deviceUuid";
    public static final String FIXES = "fixes";
    public static final String LON_DEG = "longitude";
    public static final String LAT_DEG = "latitude";
    public static final String TIME_MILLIS = "timestamp";
    public static final String SPEED_M_PER_S = "speed";
    public static final String BEARING_DEG = "course";

    @Override
    public Pair<UUID, List<GPSFixMoving>> deserialize(JSONObject object) throws JsonDeserializationException {
        UUID device = UUID.fromString(object.get(DEVICE_UUID).toString());
        JSONArray jsonFixes = Helpers.getNestedArraySafe(object, FIXES);
        List<GPSFixMoving> fixes = new ArrayList<GPSFixMoving>();
        for (int i = 0; i < jsonFixes.size(); i++) {
            JSONObject fixObject = Helpers.toJSONObjectSafe(jsonFixes.get(i));
            double lonDeg = Double.parseDouble(fixObject.get(LON_DEG).toString());
            double latDeg = Double.parseDouble(fixObject.get(LAT_DEG).toString());
            long timeMillis = Long.parseLong(fixObject.get(TIME_MILLIS).toString());
            double speedMperS = Double.parseDouble(fixObject.get(SPEED_M_PER_S).toString());
            double speedKnots = new MeterPerSecondSpeedImpl(speedMperS).getKnots();
            double bearingDeg = Double.parseDouble(fixObject.get(BEARING_DEG).toString());
            GPSFixMoving fix = GPSFixMovingImpl.create(lonDeg, latDeg, timeMillis, speedKnots, bearingDeg);
            fixes.add(fix);
        }

        return new Pair<UUID, List<GPSFixMoving>>(device, fixes);
    }
}
