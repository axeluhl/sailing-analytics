package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.TrackingConnectorInfo;
import com.sap.sailing.domain.tracking.impl.TrackingConnectorInfoImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.TrackingConnectorInfoJsonSerializer;

public class TrackingConnectorInfoJsonDeserializer implements JsonDeserializer<TrackingConnectorInfo> {

    public TrackingConnectorInfo deserialize(JSONObject object) throws JsonDeserializationException {
        String trackedBy = Helpers.getNestedObjectSafe(object, TrackingConnectorInfoJsonSerializer.FIELD_TRACKED_BY).toJSONString();
        URL webUrl;
        try {
            webUrl = new URL( (String) object.get(TrackingConnectorInfoJsonSerializer.FIELD_WEB_URL));
        } catch (MalformedURLException e) {
            throw new JsonDeserializationException(e);
        }
        TrackingConnectorInfo trackingConnectorInfo = new TrackingConnectorInfoImpl(trackedBy, webUrl);
        return trackingConnectorInfo;
    }
}
