package com.sap.sailing.selenium.api.core;

import org.json.simple.JSONObject;

public class GpsFix extends JsonWrapper {

    private static final String ATTRIBUTE_LONGITUDE = "longitude";
    private static final String ATTRIBUTE_LATITUDE = "latitude";
    private static final String ATTRIBUTE_TIMESTAMP = "timestamp";

    public static GpsFix createFix(final Double longitude, final Double latitude, final Long timestamp) {
        return new GpsFix(longitude, latitude, timestamp);
    }

    GpsFix(final Double longitude, final Double latitude, final Long timestamp) {
        super(new JSONObject());
        getJson().put(ATTRIBUTE_LONGITUDE, longitude);
        getJson().put(ATTRIBUTE_LATITUDE, latitude);
        getJson().put(ATTRIBUTE_TIMESTAMP, timestamp);
    }

    public final Double getLongitude() {
        return get(ATTRIBUTE_LONGITUDE);
    }

    public final Double getLatitude() {
        return get(ATTRIBUTE_LATITUDE);
    }

    public final Long getTimestamp() {
        return get(ATTRIBUTE_TIMESTAMP);
    }

}
