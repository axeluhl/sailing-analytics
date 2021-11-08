package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.SmartphoneUUIDSerializationHandler;
import com.sap.sailing.shared.persistence.device.DeviceIdentifierMongoHandler;
import com.sap.sse.common.TransformationException;

public class SmartphoneUUIDMongoHandler extends SmartphoneUUIDSerializationHandler
implements DeviceIdentifierMongoHandler {

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        return deserialize((String) serialized, type, stringRepresentation);
    }
    
}
