package com.sap.sailing.domain.racelogtracking;

import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sse.common.Util;

public class SmartphoneUUIDSerializationHandler {
    private SmartphoneUUIDIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof SmartphoneUUIDIdentifier))
            throw new TransformationException("Expected a SmartphoneUUIDIdentifier, got instead: " + identifier);
        return (SmartphoneUUIDIdentifier) identifier;
    }

    public Util.Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return new Util.Pair<String, String>(SmartphoneUUIDIdentifier.TYPE, castIdentifier(deviceIdentifier).getUUID().toString());
    }

    public DeviceIdentifier deserialize(String input, String type, String stringRep) throws TransformationException {
        try {
            return new SmartphoneUUIDIdentifierImpl(UUID.fromString(input));
        } catch (IllegalArgumentException e) {
            throw new TransformationException("Invalid string representation of smartphone UUID", e);
        }
    }
}
