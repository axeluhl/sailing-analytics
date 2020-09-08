package com.sap.sailing.server.gateway.serialization.impl;

import java.net.URL;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.TrackingConnectorInfo;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class TrackingConnectorInfoJsonSerializer implements JsonSerializer<TrackingConnectorInfo> {
    public static final String FIELD_TRACKING_CONNECTOR_TYPE = "trackingConnectorType";
    public static final String FIELD_WEB_URL = "webUrl";
    public static final String FIELD_CONNECTOR_DEFAULT_URL = "trackingConnectorDefaultUrl";

    @Override
    public JSONObject serialize(TrackingConnectorInfo trackingConnectorInfo) {
        JSONObject result = new JSONObject();
        URL webUrl = trackingConnectorInfo.getWebUrl();
        result.put(FIELD_TRACKING_CONNECTOR_TYPE, trackingConnectorInfo.getTrackingConnectorType().name());
        result.put(FIELD_CONNECTOR_DEFAULT_URL, trackingConnectorInfo.getTrackingConnectorType().getDefaultUrl());
        if (webUrl != null) {
            result.put(FIELD_WEB_URL, webUrl.toString());
        }
        return result;
    }
}
