package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.StorablePositioning;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class StorablePositioningJsonSerializer implements JsonSerializer<StorablePositioning> {

    public static final String FIELD_POSITION = "position";
    public static final String FIELD_DEVICE_UUID = "deviceUUID";

    private final PositionJsonSerializer positioningJsonSerializer = new PositionJsonSerializer();

    @Override
    public JSONObject serialize(StorablePositioning positioning) {
        final JSONObject result;
        if (positioning == null) {
            result = null;
        } else {
            result = new JSONObject();
            if (positioning.getDeviceUUID() != null) {
                result.put(FIELD_DEVICE_UUID, positioning.getDeviceUUID());
            }
            if (positioning.getPosition() != null) {
                result.put(FIELD_POSITION, positioningJsonSerializer.serialize(positioning.getPosition()));
            }
        }
        return result;
    }

}
