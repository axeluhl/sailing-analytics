package com.sap.sailing.expeditionconnector.impl;

import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;

public class ExpeditionGpsDeviceIdentifierJsonHandler extends ExpeditionGpsDeviceIdentifierSerializationHandler 
implements DeviceIdentifierJsonHandler {

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        return deserialize((String) serialized, type, stringRepresentation);
    }

}
