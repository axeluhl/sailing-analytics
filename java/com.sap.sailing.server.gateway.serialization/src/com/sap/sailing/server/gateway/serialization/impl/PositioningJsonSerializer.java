package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class PositioningJsonSerializer implements JsonSerializer<Positioning> {

    private static final String FIELD_TYPE = "type";
    private static final String FIELD_POSITION = "position";

    private final PositionJsonSerializer positioningJsonSerializer = new PositionJsonSerializer();

    @Override
    public JSONObject serialize(Positioning positioning) {
        final JSONObject result;
        if (positioning == null) {
            result = null;
        } else {
            result = new JSONObject();
            result.put(FIELD_TYPE, positioning.getType().toString());
            if (positioning.getPosition() != null) {
                result.put(FIELD_POSITION, positioningJsonSerializer.serialize(positioning.getPosition()));
            }
        }
        return result;
    }

}
