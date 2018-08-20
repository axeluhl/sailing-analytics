package com.sap.sailing.expeditionconnector.persistence.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.expeditionconnector.persistence.ExpeditionSensorDeviceIdentifierSerializationHandler;

public class ExpeditionSensorDeviceIdentifierMongoHandler extends ExpeditionSensorDeviceIdentifierSerializationHandler
implements DeviceIdentifierMongoHandler {
    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        return deserialize((String) serialized, type, stringRepresentation);
    }
}
