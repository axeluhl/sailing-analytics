package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifierStringSerializationHandler;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sse.common.Util;

public class TrackFileImportDeviceIdentifierStringSerializationHandler implements
        DeviceIdentifierStringSerializationHandler {

    @Override
    public Util.Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        TrackFileImportDeviceIdentifier id = TrackFileImportDeviceIdentifierImpl.cast(deviceIdentifier);
        return new Util.Pair<String, String>(TrackFileImportDeviceIdentifier.TYPE, id.getStringRepresentation());
    }

    @Override
    /**
     * Only uses the first 36 characters in the string to construct the UUID, ignoring file, track and date info.
     */
    public DeviceIdentifier deserialize(String serialized, String type, String stringRepresentation)
            throws TransformationException {
        UUID uuid = UUID.fromString(serialized.substring(0, 36));
        return TrackFileImportDeviceIdentifierImpl.getOrCreate(uuid);
    }

}
