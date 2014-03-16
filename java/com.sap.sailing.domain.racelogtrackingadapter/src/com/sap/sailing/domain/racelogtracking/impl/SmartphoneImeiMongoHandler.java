package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.impl.SmartphoneImeiSerializationHandler;

public class SmartphoneImeiMongoHandler extends SmartphoneImeiSerializationHandler
implements DeviceIdentifierMongoHandler {

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        return deserialize((String) serialized, type, stringRepresentation);
    }
    
}
