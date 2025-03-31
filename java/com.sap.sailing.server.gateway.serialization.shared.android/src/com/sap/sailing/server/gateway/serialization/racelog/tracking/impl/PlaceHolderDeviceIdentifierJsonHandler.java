package com.sap.sailing.server.gateway.serialization.racelog.tracking.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.impl.PlaceHolderDeviceIdentifierSerializationHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sse.common.TransformationException;

public class PlaceHolderDeviceIdentifierJsonHandler extends PlaceHolderDeviceIdentifierSerializationHandler
implements DeviceIdentifierJsonHandler {

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        return deserialize((String) serialized, type, stringRepresentation);
    }

}
