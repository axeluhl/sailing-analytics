package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifierImpl;
import com.sap.sse.common.Util;

public class PingDeviceIdentifierSerializationHandler {
    private PingDeviceIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof PingDeviceIdentifier))
            throw new TransformationException("Expected a PingDeviceIdentifier, got instead: " + identifier);
        return (PingDeviceIdentifier) identifier;
    }

    public Util.Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return new Util.Pair<String, String>(PingDeviceIdentifier.TYPE, castIdentifier(deviceIdentifier).getId().toString());
    }

    public DeviceIdentifier deserialize(String input, String type, String stringRep) throws TransformationException {
        try {
            return new PingDeviceIdentifierImpl(UUID.fromString(stringRep));
        } catch (IllegalArgumentException e) {
            throw new TransformationException(e);
        }
    }
}
