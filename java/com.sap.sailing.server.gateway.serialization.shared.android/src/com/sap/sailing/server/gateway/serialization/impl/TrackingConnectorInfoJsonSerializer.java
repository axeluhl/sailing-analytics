package com.sap.sailing.server.gateway.serialization.impl;

import java.net.URL;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.TrackingConnectorInfo;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class TrackingConnectorInfoJsonSerializer implements JsonSerializer<TrackingConnectorInfo> {
    public static final String FIELD_TRACKED_BY = "trackedBy";
    public static final String FIELD_WEB_URL = "webUrl";

    @Override
    public JSONObject serialize(TrackingConnectorInfo trackingConnectorInfo) {
        JSONObject result = new JSONObject();
        URL webUrl = trackingConnectorInfo.getWebUrl();
        result.put(FIELD_TRACKED_BY, trackingConnectorInfo.getTrackedBy());
        if (webUrl != null) {
            result.put(FIELD_WEB_URL, webUrl.toString());
        }
        return result;
    }
}
