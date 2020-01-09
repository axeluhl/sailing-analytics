package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.impl.FixedPositioningImpl;
import com.sap.sailing.domain.coursetemplate.impl.TrackingDeviceBasedPositioningImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.PositioningJsonSerializer;

public class PositioningJsonDeserializer implements JsonDeserializer<Positioning> {
    private static final Logger logger = Logger.getLogger(PositioningJsonDeserializer.class.getName());
    
    private final PositionJsonDeserializer positionDeserializer;
    private final DeviceIdentifierJsonDeserializer deviceIdentifierDeserializer;
    
    public PositioningJsonDeserializer(DeviceIdentifierJsonDeserializer deviceIdentifierDeserializer) {
        this.positionDeserializer = new PositionJsonDeserializer();
        this.deviceIdentifierDeserializer = deviceIdentifierDeserializer;
    }

    @Override
    public Positioning deserialize(JSONObject object) throws JsonDeserializationException {
        final Positioning result;
        if (object.containsKey(PositioningJsonSerializer.FIELD_POSITION)) {
            result = new FixedPositioningImpl(positionDeserializer
                    .deserialize((JSONObject) object.get(PositioningJsonSerializer.FIELD_POSITION)));
        } else if (object.containsKey(PositioningJsonSerializer.FIELD_DEVICE_IDENTIFIER)) {
            final DeviceIdentifier deviceIdentifier = deviceIdentifierDeserializer
                    .deserialize((JSONObject) object.get(PositioningJsonSerializer.FIELD_DEVICE_IDENTIFIER));
            result = new TrackingDeviceBasedPositioningImpl(deviceIdentifier);
        } else {
            logger.warning("Unknown Positioning object type: "+object);
            result = null;
        }
        return result;
    }
}
