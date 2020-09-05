package com.sap.sailing.server.gateway.serialization.impl;

import java.net.URL;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.TrackingConnectorInfo;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class TrackingConnectorInfoJsonSerializer implements JsonSerializer<TrackingConnectorInfo> {
    public static final String FIELD_TRACKING_CONNECTOR_TYPE = "trackingConnectorType";
    public static final String FIELD_WEB_URL = "webUrl";
	public static final String FIELD_UUID = "uuid";

    @Override
    public JSONObject serialize(TrackingConnectorInfo trackingConnectorInfo) {
        JSONObject result = new JSONObject();
        URL webUrl = trackingConnectorInfo.getWebUrl();
        result.put(FIELD_TRACKING_CONNECTOR_TYPE, trackingConnectorInfo.getTrackingConnectorType().name());
        if (webUrl != null) {
            result.put(FIELD_WEB_URL, webUrl.toString());
        }
        UUID uuid = trackingConnectorInfo.getUuid();
        if (uuid != null) {
        	result.put(FIELD_UUID, uuid.toString());
        }
        return result;
    }
}
