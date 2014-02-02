package com.sap.sailing.domain.smartphoneadapter.impl;

import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.persistence.devices.DeviceIdentifierPersistenceHandler;

public class SmartphoneImeiHandlerImpl implements DeviceIdentifierPersistenceHandler {
//    private static final Logger logger = Logger.getLogger(SmartphoneImeiHandlerImpl.class.getName());

    @Override
    public Object store(DeviceIdentifier deviceIdentifier) throws IllegalArgumentException {
        if (!(deviceIdentifier instanceof SmartphoneImeiIdentifier)) {
            throw new IllegalArgumentException(String.format(
                    "Unexpected device identifier of type %s while trying to persist smartphone IMEI identifier",
                    deviceIdentifier.getClass().getName()));
        }
        SmartphoneImeiIdentifier imeiIdentifier = (SmartphoneImeiIdentifier) deviceIdentifier;
        return imeiIdentifier.getImei();
    }

    @Override
    public SmartphoneImeiIdentifier load(Object input) {
        return new SmartphoneImeiIdentifier((String) input);
    }
}
