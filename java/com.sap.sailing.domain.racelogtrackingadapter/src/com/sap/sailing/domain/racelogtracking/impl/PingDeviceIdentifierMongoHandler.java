package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;

public class PingDeviceIdentifierMongoHandler extends PingDeviceIdentifierSerializationHandler
implements DeviceIdentifierMongoHandler {

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        return deserialize((String) serialized, type, stringRepresentation);
    }

}
