package com.sap.sailing.domain.racelog.tracking.impl;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifierSerializationHandler;
import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;

public class SmartphoneImeiSerializationHandler implements DeviceIdentifierSerializationHandler<Object> {
    private SmartphoneImeiIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof SmartphoneImeiIdentifier))
            throw new TransformationException("Expected a SmartphoneImeiIdentifier, got instead: " + identifier);
        return (SmartphoneImeiIdentifier) identifier;
    }

    @Override
    public Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return new Pair<String, String>(castIdentifier(deviceIdentifier).getImei(), SmartphoneImeiIdentifier.TYPE);
    }

    @Override
    public DeviceIdentifier deserialize(Object input, String type, String stringRep) {
        return new SmartphoneImeiIdentifier((String) input);
    }
}
