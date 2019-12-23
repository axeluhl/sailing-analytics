package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.FixedPositioning;
import com.sap.sailing.domain.coursetemplate.Positioning;
import com.sap.sailing.domain.coursetemplate.PositioningVisitor;
import com.sap.sailing.domain.coursetemplate.TrackingDeviceBasedPositioning;
import com.sap.sailing.domain.coursetemplate.TrackingDeviceBasedPositioningWithLastKnownPosition;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class PositioningJsonSerializer implements JsonSerializer<Positioning> {
    public static final String FIELD_POSITION = "position";
    public static final String FIELD_DEVICE_IDENTIFIER = "device_identifier";
    public static final String FIELD_DEVICE_LAST_KNOWN_POSITION = "device_last_known_position";

    private final PositionJsonSerializer positionJsonSerializer = new PositionJsonSerializer();
    private final DeviceIdentifierJsonSerializer deviceIdentifierJsonSerializer;

    
    public PositioningJsonSerializer(DeviceIdentifierJsonSerializer deviceIdentifierJsonSerializer) {
        super();
        this.deviceIdentifierJsonSerializer = deviceIdentifierJsonSerializer;
    }

    @Override
    public JSONObject serialize(Positioning positioning) {
        final JSONObject result;
        if (positioning == null) {
            result = null;
        } else {
            result = positioning.accept(new PositioningVisitor<JSONObject>() {
                @Override
                public JSONObject visit(FixedPositioning fixedPositioning) {
                    final JSONObject result = new JSONObject();
                    result.put(FIELD_POSITION, positionJsonSerializer.serialize(fixedPositioning.getFixedPosition()));
                    return result;
                }

                @Override
                public JSONObject visit(TrackingDeviceBasedPositioning trackingDeviceBasedPositioning) {
                    final JSONObject result = new JSONObject();
                    result.put(FIELD_DEVICE_IDENTIFIER, deviceIdentifierJsonSerializer.serialize(
                            trackingDeviceBasedPositioning.getDeviceIdentifier()));
                    return result;
                }

                @Override
                public JSONObject visit(
                        TrackingDeviceBasedPositioningWithLastKnownPosition trackingDeviceBasedPositioningWithLastKnownPosition) {
                    final JSONObject result = visit((TrackingDeviceBasedPositioning) trackingDeviceBasedPositioningWithLastKnownPosition);
                    result.put(FIELD_DEVICE_LAST_KNOWN_POSITION, positionJsonSerializer.serialize(
                            trackingDeviceBasedPositioningWithLastKnownPosition.getLastKnownPosition()));
                    return result;
                }
            });
        }
        return result;
    }
}
