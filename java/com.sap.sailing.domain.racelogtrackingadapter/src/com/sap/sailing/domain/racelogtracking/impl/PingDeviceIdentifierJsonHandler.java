package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;

public class PingDeviceIdentifierJsonHandler extends PingDeviceIdentifierSerializationHandler 
implements DeviceIdentifierJsonHandler {

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        return deserialize((String) serialized, type, stringRepresentation);
    }

}
