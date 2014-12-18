package com.sap.sailing.domain.persistence.racelog.tracking.impl;

import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.PlaceHolderDeviceIdentifierSerializationHandler;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;

public class PlaceHolderDeviceIdentifierMongoHandler extends PlaceHolderDeviceIdentifierSerializationHandler
implements DeviceIdentifierMongoHandler {

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        return deserialize((String) serialized, type, stringRepresentation);
    }

}
