package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.PingDeviceIdentifierImpl;

public class PingDeviceMongoHandlerImpl implements DeviceIdentifierMongoHandler {
    private PingDeviceIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof PingDeviceIdentifier))
            throw new TransformationException("Expected a PingDeviceIdentifier, got instead: " + identifier);
        return (PingDeviceIdentifier) identifier;
    }

    @Override
    public Object transformForth(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return castIdentifier(deviceIdentifier).getId().toString();
    }

    @Override
    public DeviceIdentifier transformBack(Object input) {
        return new PingDeviceIdentifierImpl(UUID.fromString((String) input));
    }
}
