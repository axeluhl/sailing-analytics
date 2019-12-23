package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.StoredDeviceIdentifierPositioning;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class StoredDeviceIdentifierPositioningJsonSerializer implements JsonSerializer<StoredDeviceIdentifierPositioning> {

    public static final String FIELD_POSITION = "position";
    public static final String FIELD_DEVICE_UUID = "deviceUUID";

    private final PositionJsonSerializer positioningJsonSerializer = new PositionJsonSerializer();

    @Override
    public JSONObject serialize(StoredDeviceIdentifierPositioning positioning) {
        final JSONObject result;
        if (positioning == null) {
            result = null;
        } else {
            result = new JSONObject();
            if (positioning.getDeviceIdentifier() != null) {
                result.put(FIELD_DEVICE_UUID, positioning.getDeviceIdentifier().getStringRepresentation());
            }
            if (positioning.getPosition() != null) {
                result.put(FIELD_POSITION, positioningJsonSerializer.serialize(positioning.getPosition()));
            }
        }
        return result;
    }

}
