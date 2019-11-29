package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.TrackingConnectorInfo;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class TrackingConnectorInfoJsonSerializer implements JsonSerializer<TrackingConnectorInfo> {
    public static final String FIELD_TRACKED_BY = "trackedBy";
    public static final String FIELD_WEB_URL = "webUrl";

    @Override
    public JSONObject serialize(TrackingConnectorInfo trackingConnectorInfo) {
        JSONObject result = new JSONObject();
        result.put(FIELD_TRACKED_BY, trackingConnectorInfo.getTrackedBy());
        result.put(FIELD_WEB_URL, trackingConnectorInfo.getWebUrl());
        return result;
    }
}

