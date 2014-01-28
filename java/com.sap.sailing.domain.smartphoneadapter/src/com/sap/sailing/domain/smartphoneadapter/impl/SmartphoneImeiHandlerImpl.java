package com.sap.sailing.domain.smartphoneadapter.impl;

import com.mongodb.DBObject;
import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.persistence.devices.DeviceIdentifierPersistenceHandler;

public class SmartphoneImeiHandlerImpl implements DeviceIdentifierPersistenceHandler {
//    private static final Logger logger = Logger.getLogger(SmartphoneImeiHandlerImpl.class.getName());

    public static final String FIELD_IMEI = "imei";

    @Override
    public DBObject enrichDBObject(DBObject base, DeviceIdentifier deviceIdentifier) throws IllegalArgumentException {
        if (!(deviceIdentifier instanceof SmartphoneImeiIdentifier)) {
            throw new IllegalArgumentException(String.format(
                    "Unexpected device identifier of type %s while trying to persist smartphone IMEI identifier",
                    deviceIdentifier.getClass().getName()));
        }
        SmartphoneImeiIdentifier imeiIdentifier = (SmartphoneImeiIdentifier) deviceIdentifier;
        base.put(FIELD_IMEI, imeiIdentifier.getImei());
        return base;
    }

    @Override
    public SmartphoneImeiIdentifier loadFromDBObject(DBObject input) {
        return new SmartphoneImeiIdentifier((String) input.get(FIELD_IMEI));
    }
}
