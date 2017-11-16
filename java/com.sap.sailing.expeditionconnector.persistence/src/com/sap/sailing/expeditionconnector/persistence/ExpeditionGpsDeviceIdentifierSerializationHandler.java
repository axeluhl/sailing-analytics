package com.sap.sailing.expeditionconnector.persistence;

import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sse.common.Util;

public class ExpeditionGpsDeviceIdentifierSerializationHandler {
    private ExpeditionGpsDeviceIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof ExpeditionGpsDeviceIdentifier))
            throw new TransformationException("Expected a ExpeditionGpsDeviceIdentifier, got instead: " + identifier);
        return (ExpeditionGpsDeviceIdentifier) identifier;
    }

    public Util.Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return new Util.Pair<String, String>(ExpeditionGpsDeviceIdentifier.TYPE, castIdentifier(deviceIdentifier).getId().toString());
    }

    public DeviceIdentifier deserialize(String input, String type, String stringRep) throws TransformationException {
        try {
            return new ExpeditionGpsDeviceIdentifierImpl(UUID.fromString(input));
        } catch (IllegalArgumentException e) {
            throw new TransformationException("Invalid string representation of smartphone UUID", e);
        }
    }
}
