package com.sap.sailing.domain.racelog.tracking.impl;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;

public class SmartphoneImeiSerializationHandler {
    private SmartphoneImeiIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof SmartphoneImeiIdentifier))
            throw new TransformationException("Expected a SmartphoneImeiIdentifier, got instead: " + identifier);
        return (SmartphoneImeiIdentifier) identifier;
    }

    public Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return new Pair<String, String>(castIdentifier(deviceIdentifier).getImei(), SmartphoneImeiIdentifier.TYPE);
    }

    public DeviceIdentifier deserialize(String input, String type, String stringRep) {
        return new SmartphoneImeiIdentifier(input);
    }
}
