package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifierSerializationHandler;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifierImpl;

public class PingDeviceIdentifierSerializationHandler implements DeviceIdentifierSerializationHandler<Object> {
    private PingDeviceIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof PingDeviceIdentifier))
            throw new TransformationException("Expected a PingDeviceIdentifier, got instead: " + identifier);
        return (PingDeviceIdentifier) identifier;
    }

    @Override
    public Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return new Pair<String, String>(castIdentifier(deviceIdentifier).getId().toString(), PingDeviceIdentifier.TYPE);
    }

    @Override
    public DeviceIdentifier deserialize(Object input, String type, String stringRep) {
        return new PingDeviceIdentifierImpl(UUID.fromString((String) input));
    }
}
