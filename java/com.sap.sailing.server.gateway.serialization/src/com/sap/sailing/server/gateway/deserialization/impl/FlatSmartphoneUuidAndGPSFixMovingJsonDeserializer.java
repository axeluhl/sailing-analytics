package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.MeterPerSecondSpeedImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.Util.Pair;

/**
 * Make serialization on the smartphone easier by providing a flat structure rather
 * than nested JSON documents.
 * @author Fredrik Teschke
 *
 */
public class FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer implements JsonDeserializer<Pair<UUID, GPSFixMoving>> {
    public static final String DEVICE_UUID = "deviceUuid";
    public static final String LON_DEG = "lonDeg";
    public static final String LAT_DEG = "latDeg";
    public static final String TIME_MILLIS = "timeMillis";
    public static final String SPEED_M_PER_S = "speedMperS";
    public static final String BEARING_DEG = "bearingDeg";
    public static final String ACCURACY = "accuracy";
    public static final String ALTITUDE = "altitude";
    public static final String PROVIDER = "provider";

    @Override
    public Pair<UUID, GPSFixMoving> deserialize(JSONObject object) throws JsonDeserializationException {
        UUID device = UUID.fromString(object.get(DEVICE_UUID).toString());
        double lonDeg = Double.parseDouble(object.get(LON_DEG).toString());
        double latDeg = Double.parseDouble(object.get(LAT_DEG).toString());
        long timeMillis = Long.parseLong(object.get(TIME_MILLIS).toString());
        double speedMperS = Double.parseDouble(object.get(SPEED_M_PER_S).toString());
        double speedKnots = new MeterPerSecondSpeedImpl(speedMperS).getKnots();
        double bearingDeg = Double.parseDouble(object.get(BEARING_DEG).toString());
        double accuracy = Double.parseDouble(object.get(ACCURACY).toString());
        double altitude = Double.parseDouble(object.get(ALTITUDE).toString());
        String provider = object.get(PROVIDER).toString();
        GPSFixMoving fix = GPSFixMovingImpl.create(lonDeg, latDeg, timeMillis, speedKnots, bearingDeg, accuracy, altitude, provider);
        return new Pair<UUID, GPSFixMoving>(device, fix);
    }
}
