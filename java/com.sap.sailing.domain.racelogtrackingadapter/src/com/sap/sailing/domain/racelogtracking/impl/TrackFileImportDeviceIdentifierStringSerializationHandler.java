package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifierStringSerializationHandler;
import com.sap.sailing.server.gateway.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.server.gateway.trackfiles.TrackFileImportDeviceIdentifierImpl;

public class TrackFileImportDeviceIdentifierStringSerializationHandler implements
        DeviceIdentifierStringSerializationHandler {

    @Override
    public Pair<String, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        TrackFileImportDeviceIdentifier id = TrackFileImportDeviceIdentifierImpl.cast(deviceIdentifier);
        return new Pair<String, String>(id.toString(), TrackFileImportDeviceIdentifier.TYPE);
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
