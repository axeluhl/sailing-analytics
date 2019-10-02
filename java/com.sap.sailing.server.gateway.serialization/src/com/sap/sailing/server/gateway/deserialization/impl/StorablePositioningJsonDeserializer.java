package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.StorablePositioning;
import com.sap.sailing.domain.coursetemplate.impl.FixedPositioningImpl;
import com.sap.sailing.domain.coursetemplate.impl.SmartphoneUUIDPositioningImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.StorablePositioningJsonSerializer;

public class StorablePositioningJsonDeserializer implements JsonDeserializer<StorablePositioning> {

    private final PositionJsonDeserializer positionJsonDeserializer = new PositionJsonDeserializer();

    @Override
    public StorablePositioning deserialize(JSONObject object) throws JsonDeserializationException {
        final StorablePositioning result;
        if (object == null) {
            result = null;
        } else {
            final Position position;
            final Object positionObject = object.get(StorablePositioningJsonSerializer.FIELD_POSITION);
            if (positionObject instanceof JSONObject) {
                position = positionJsonDeserializer.deserialize((JSONObject) positionObject);
            } else {
                position = null;
            }
            final String deviceUUIDString = (String) object.get(StorablePositioningJsonSerializer.FIELD_DEVICE_UUID);
            if (deviceUUIDString != null) {
                result = new SmartphoneUUIDPositioningImpl(UUID.fromString(deviceUUIDString), position);
            } else if (position != null) {
                result = new FixedPositioningImpl(position);
            } else {
                result = null;
            }
        }
        return result;
    }

}
