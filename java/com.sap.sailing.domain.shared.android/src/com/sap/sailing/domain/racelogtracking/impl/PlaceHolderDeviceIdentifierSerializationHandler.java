package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelogtracking.PlaceHolderDeviceIdentifier;
import com.sap.sse.common.Util;

/**
 * Can be used as the last option, if no other service is available when trying to load an identifier
 * from, or save it to the database.
 * The handler itself doesn't do much, but relies on the {@code type} and {@code stringRepresentation}
 * to be saved and loaded. This way, the {@code stringRepresentation} is always available, even if the
 * appropriate persistence service was available when writing, and is now unavailable when loading.
 * 
 * @author Fredrik Teschke
 *
 */
public class PlaceHolderDeviceIdentifierSerializationHandler {

    public DeviceIdentifier deserialize(String object, String type, String stringRepresentation) throws TransformationException {
        return new PlaceHolderDeviceIdentifier(type, stringRepresentation);
    }

    public Util.Pair<String, String> serialize(DeviceIdentifier object) throws TransformationException {
        return new Util.Pair<String, String>(object.getIdentifierType(), object.getStringRepresentation());
    }

}
