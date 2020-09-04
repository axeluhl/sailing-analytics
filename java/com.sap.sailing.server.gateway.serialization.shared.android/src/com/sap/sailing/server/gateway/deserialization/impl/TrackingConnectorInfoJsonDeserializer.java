package com.sap.sailing.server.gateway.deserialization.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.tracking.TrackingConnectorType;
import com.sap.sailing.domain.tracking.TrackingConnectorInfo;
import com.sap.sailing.domain.tracking.impl.TrackingConnectorInfoImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.TrackingConnectorInfoJsonSerializer;

public class TrackingConnectorInfoJsonDeserializer implements JsonDeserializer<TrackingConnectorInfo> {
    private static final Logger logger = Logger.getLogger(TrackingConnectorInfoJsonDeserializer.class.getName());

    public TrackingConnectorInfo deserialize(JSONObject object) throws JsonDeserializationException {
        final TrackingConnectorType trackingConnectorType = TrackingConnectorType
                .valueOf((String) object.get(TrackingConnectorInfoJsonSerializer.FIELD_TRACKING_CONNECTOR_TYPE));
        final String webUrlString = (String) object.get(TrackingConnectorInfoJsonSerializer.FIELD_WEB_URL);
        URL webUrl;
        try {
            webUrl = webUrlString == null ? null : new URL(webUrlString);
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Error while parsing webUrl of TrackingConnectorInfo", e);
            webUrl = null;
        }
        final String uuidString = (String) object.get(TrackingConnectorInfoJsonSerializer.FIELD_UUID);
        UUID uuid;
        try {
            uuid = uuidString == null ? null : UUID.fromString(uuidString);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while parsing uuid of TrackingConnectorInfo", e);
            uuid = null;
        }
        final TrackingConnectorInfo trackingConnectorInfo = new TrackingConnectorInfoImpl(trackingConnectorType,
                webUrl, uuid);
        return trackingConnectorInfo;
    }
}
