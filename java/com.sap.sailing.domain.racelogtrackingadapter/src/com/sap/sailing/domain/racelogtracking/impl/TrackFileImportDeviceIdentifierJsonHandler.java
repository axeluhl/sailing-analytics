package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.server.gateway.trackfiles.TrackFileImportDeviceIdentifierImpl;

public class TrackFileImportDeviceIdentifierJsonHandler implements DeviceIdentifierJsonHandler {
    private static enum Fields {UUID, FILE_NAME, TRACK_NAME, DATE_AS_MILLIS};

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        JSONObject json = (JSONObject) serialized;
        UUID uuid = UUID.fromString((String) json.get(Fields.UUID.name()));
        String fileName = (String) json.get(Fields.FILE_NAME.name());
        String trackName = (String) json.get(Fields.TRACK_NAME.name());
        Long dateAsMillis = (Long) json.get(Fields.DATE_AS_MILLIS.name());
        TimePoint timePoint = dateAsMillis == null ? null : new MillisecondsTimePoint(dateAsMillis);
        return new TrackFileImportDeviceIdentifierImpl(uuid, fileName, trackName, timePoint);
    }

    @Override
    public Pair<JSONObject, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        TrackFileImportDeviceIdentifier id = TrackFileImportDeviceIdentifierImpl.cast(deviceIdentifier);
        JSONObject result = new JSONObject();
        result.put(Fields.UUID.name(), id.getId().toString());
        result.put(Fields.FILE_NAME.name(), id.getFileName());
        result.put(Fields.TRACK_NAME.name(), id.getTrackName());
        result.put(Fields.DATE_AS_MILLIS.name(), id.getUploadedAt() == null ? null : id.getUploadedAt().asMillis());
        return new Pair<JSONObject, String>(result, TrackFileImportDeviceIdentifier.TYPE);
    }

}
