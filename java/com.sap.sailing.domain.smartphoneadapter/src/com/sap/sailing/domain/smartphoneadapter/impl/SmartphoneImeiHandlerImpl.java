package com.sap.sailing.domain.smartphoneadapter.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sailing.domain.devices.DeviceIdentifier;
import com.sap.sailing.domain.devices.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.persistence.devices.DeviceIdentifierPersistenceHandler;

public class SmartphoneImeiHandlerImpl implements DeviceIdentifierPersistenceHandler {
//    private static final Logger logger = Logger.getLogger(SmartphoneImeiHandlerImpl.class.getName());

    public static final String FIELD_IMEI = "imei";

    @Override
    public DBObject store(DeviceIdentifier deviceIdentifier) throws IllegalArgumentException {
        if (!(deviceIdentifier instanceof SmartphoneImeiIdentifier)) {
            throw new IllegalArgumentException(String.format(
                    "Unexpected device identifier of type %s while trying to persist smartphone IMEI identifier",
                    deviceIdentifier.getClass().getName()));
        }
        DBObject result = new BasicDBObject();
        SmartphoneImeiIdentifier imeiIdentifier = (SmartphoneImeiIdentifier) deviceIdentifier;
        result.put(FIELD_IMEI, imeiIdentifier.getImei());
        return result;
    }

    @Override
    public SmartphoneImeiIdentifier load(DBObject input) {
        return new SmartphoneImeiIdentifier((String) input.get(FIELD_IMEI));
    }
}
