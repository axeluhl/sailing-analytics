package com.sap.sailing.domain.racelog.tracking;

import java.util.UUID;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.impl.SmartphoneUUIDIdentifierImpl;

public class SmartphoneUUIDSerializationHandler {
    private SmartphoneUUIDIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof SmartphoneUUIDIdentifier))
            throw new TransformationException("Expected a SmartphoneUUIDIdentifier, got instead: " + identifier);
        return (SmartphoneUUIDIdentifier) identifier;
    }

    public Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return new Pair<String, String>(castIdentifier(deviceIdentifier).getUUID().toString(), SmartphoneUUIDIdentifier.TYPE);
    }

    public DeviceIdentifier deserialize(String input, String type, String stringRep) {
        return new SmartphoneUUIDIdentifierImpl(UUID.fromString(input));
    }
}
