package com.sap.sailing.domain.racelog.tracking.impl;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.SmartphoneImeiIdentifier;

public class SmartphoneImeiMongoHandlerImpl implements DeviceIdentifierMongoHandler {
    private SmartphoneImeiIdentifier castIdentifier(DeviceIdentifier identifier) throws TransformationException {
        if (!(identifier instanceof SmartphoneImeiIdentifier))
            throw new TransformationException("Expected a SmartphoneImeiIdentifier, got instead: " + identifier);
        return (SmartphoneImeiIdentifier) identifier;
    }

    @Override
    public Object transformForth(DeviceIdentifier deviceIdentifier) throws TransformationException {
        return castIdentifier(deviceIdentifier).getImei();
    }

    @Override
    public DeviceIdentifier transformBack(Object input) {
        return new SmartphoneImeiIdentifier((String) input);
    }
}
