package com.sap.sailing.domain.racelog.tracking;

import java.util.UUID;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sse.common.UtilNew;

public class SmartphoneUUIDSerializationHandler {
    private SmartphoneUUIDIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof SmartphoneUUIDIdentifier))
            throw new TransformationException("Expected a SmartphoneUUIDIdentifier, got instead: " + identifier);
        return (SmartphoneUUIDIdentifier) identifier;
    }

    public UtilNew.Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return new UtilNew.Pair<String, String>(castIdentifier(deviceIdentifier).getUUID().toString(), SmartphoneUUIDIdentifier.TYPE);
    }

    public DeviceIdentifier deserialize(String input, String type, String stringRep) throws TransformationException {
        try {
            return new SmartphoneUUIDIdentifierImpl(UUID.fromString(stringRep));
        } catch (IllegalArgumentException e) {
            throw new TransformationException("Invalid string representation of smartphone UUID", e);
        }
    }
}
