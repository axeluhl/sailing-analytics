package com.sap.sailing.selenium.api.coursetemplate;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class Positioning extends JsonWrapper {

    private static final String FIELD_POSITION = "position";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_DEVICE_IDENTIFIER = "device_identifier";
    private static final String FIELD_LATITUDE_DEG = "latitude_deg";
    private static final String FIELD_LONGITUDE_DEG = "longitude_deg";
    private static final String FIELD_DEVICE_ID = "id";
    private static final String FIELD_DEVICE_TYPE = "type";
    private static final String FIELD_STRING_REPRESENTATION = "stringRepresentation";


    public Positioning(final JSONObject json) {
        super(json);
    }

    public Positioning(UUID deviceId) {
        super(new JSONObject());
        final JSONObject deviceIdentifierJson = new JSONObject();
        deviceIdentifierJson.put(FIELD_DEVICE_ID, deviceId.toString());
        deviceIdentifierJson.put(FIELD_STRING_REPRESENTATION, deviceId.toString());
        deviceIdentifierJson.put(FIELD_DEVICE_TYPE, "smartphoneUUID");
        getJson().put(FIELD_DEVICE_IDENTIFIER, deviceIdentifierJson);
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
        final JSONObject deviceIdJson = get(FIELD_DEVICE_IDENTIFIER);
        return deviceIdJson != null ? UUID.fromString((String) deviceIdJson.get(FIELD_DEVICE_ID)) : null;
    }
}
