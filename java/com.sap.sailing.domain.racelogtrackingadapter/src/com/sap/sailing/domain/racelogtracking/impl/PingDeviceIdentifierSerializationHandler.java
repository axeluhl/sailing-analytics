package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifierImpl;

public class PingDeviceIdentifierSerializationHandler {
    private PingDeviceIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof PingDeviceIdentifier))
            throw new TransformationException("Expected a PingDeviceIdentifier, got instead: " + identifier);
        return (PingDeviceIdentifier) identifier;
    }

    public Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return new Pair<String, String>(castIdentifier(deviceIdentifier).getId().toString(), PingDeviceIdentifier.TYPE);
    }

    public DeviceIdentifier deserialize(String input, String type, String stringRep) {
        return new PingDeviceIdentifierImpl(UUID.fromString(input));
    }
}
