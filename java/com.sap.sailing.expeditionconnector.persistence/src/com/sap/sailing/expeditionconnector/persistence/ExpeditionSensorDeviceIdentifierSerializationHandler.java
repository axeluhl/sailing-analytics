package com.sap.sailing.expeditionconnector.persistence;

import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.expeditionconnector.ExpeditionSensorDeviceIdentifier;
import com.sap.sse.common.Util;

public class ExpeditionSensorDeviceIdentifierSerializationHandler {
    private ExpeditionSensorDeviceIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof ExpeditionSensorDeviceIdentifier))
            throw new TransformationException("Expected a ExpeditionSensorDeviceIdentifier, got instead: " + identifier);
        return (ExpeditionSensorDeviceIdentifier) identifier;
    }

    public Util.Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return new Util.Pair<String, String>(ExpeditionSensorDeviceIdentifier.TYPE, castIdentifier(deviceIdentifier).getId().toString());
    }

    public DeviceIdentifier deserialize(String input, String type, String stringRep) throws TransformationException {
        try {
            return new ExpeditionSensorDeviceIdentifierImpl(UUID.fromString(input));
        } catch (IllegalArgumentException e) {
            throw new TransformationException("Invalid string representation of smartphone UUID", e);
        }
    }
}
