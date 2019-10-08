package com.sap.sailing.selenium.api.coursetemplate;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class Positioning extends JsonWrapper {

    private static final String FIELD_POSITION = "position";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_DEVICE_UUID = "deviceUUID";
    private static final String FIELD_LATITUDE_DEG = "latitude_deg";
    private static final String FIELD_LONGITUDE_DEG = "longitude_deg";

    public Positioning(final JSONObject json) {
        super(json);
    }

    public Positioning(UUID deviceId) {
        super(new JSONObject());
        getJson().put(FIELD_DEVICE_UUID, deviceId.toString());
    }

    public Positioning(double latDeg, double lngDeg) {
        super(new JSONObject());
        JSONObject position = new JSONObject();
        position.put(FIELD_LATITUDE_DEG, latDeg);
        position.put(FIELD_LONGITUDE_DEG, lngDeg);
        getJson().put(FIELD_POSITION, position);
    }

    public String getType() {
        return (String) get(FIELD_TYPE);
    }

    public Double getLatitudeDeg() {
        final JSONObject position = get(FIELD_POSITION);
        return position != null ? (Double) position.get(FIELD_LATITUDE_DEG) : null;
    }

    public Double getLongitudeDeg() {
        final JSONObject position = get(FIELD_POSITION);
        return position != null ? (Double) position.get(FIELD_LONGITUDE_DEG) : null;
    }

    public UUID getDeviceId() {
        final String deviceIdJson = get(FIELD_DEVICE_UUID);
        return deviceIdJson != null ? UUID.fromString(deviceIdJson) : null;
    }
}
