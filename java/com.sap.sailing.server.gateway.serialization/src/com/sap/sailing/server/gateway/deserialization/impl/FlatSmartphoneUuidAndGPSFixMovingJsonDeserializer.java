package com.sap.sailing.server.gateway.deserialization.impl;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.impl.MeterPerSecondSpeedImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.FlatSmartphoneUuidAndGPSFixMovingJsonSerializer;
import com.sap.sailing.domain.common.tracking.impl.GPSFixMovingImpl;
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
    public static final String ACCURACY = "accuracy";

    @Override
    public Pair<UUID, List<GPSFixMoving>> deserialize(JSONObject object) throws JsonDeserializationException {
        UUID device = UUID.fromString(object.get(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.DEVICE_UUID).toString());
        JSONArray jsonFixes = Helpers.getNestedArraySafe(object, FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.FIXES);
        List<GPSFixMoving> fixes = new ArrayList<GPSFixMoving>();
        for (int i = 0; i < jsonFixes.size(); i++) {
            JSONObject fixObject = Helpers.toJSONObjectSafe(jsonFixes.get(i));
            double lonDeg = Double.parseDouble(fixObject.get(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.LON_DEG).toString());
            double latDeg = Double.parseDouble(fixObject.get(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.LAT_DEG).toString());
            long timeMillis = deserializeTimestamp(fixObject);
            double speedMperS = Double.parseDouble(fixObject.get(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.SPEED_M_PER_S).toString());
            double speedKnots = new MeterPerSecondSpeedImpl(speedMperS).getKnots();
            double bearingDeg = Double.parseDouble(fixObject.get(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.BEARING_DEG).toString());
            GPSFixMoving fix = GPSFixMovingImpl.create(lonDeg, latDeg, timeMillis, speedKnots, bearingDeg);
            fixes.add(fix);
        }

        return new Pair<UUID, List<GPSFixMoving>>(device, fixes);
    }

    private long deserializeTimestamp(JSONObject fixObject) throws JsonDeserializationException {
        long timeMillis;
        if (fixObject.get(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.TIME_MILLIS) != null) {
            timeMillis = Long.parseLong(
                    fixObject.get(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.TIME_MILLIS).toString());
        } else if (fixObject.get(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.TIME_ISO) != null) {
            String strIsoTimestamp = fixObject.get(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.TIME_ISO)
                    .toString();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;  
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(strIsoTimestamp, timeFormatter);
            timeMillis = offsetDateTime.toInstant().toEpochMilli();
        } else {
            throw new JsonDeserializationException("no timestamp field provided. Please provide one of these fields: "
                    + FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.TIME_MILLIS + ", "
                    + FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.TIME_ISO);
        }
        return timeMillis;
    }
}
