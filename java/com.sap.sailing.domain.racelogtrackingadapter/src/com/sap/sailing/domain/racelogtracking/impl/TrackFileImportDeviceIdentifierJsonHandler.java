package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackFileImportDeviceIdentifierJsonHandler implements DeviceIdentifierJsonHandler {
    private static enum Fields {UUID, FILE_NAME, TRACK_NAME, UPLOADED_MILLIS};
    
    public static TimePoint loadTimePoint(Object object) {
        if (object == null) {
            return null;
        }
        return new MillisecondsTimePoint(((Number) object).longValue());        
    }
    
    public static Long storeTimePoint(TimePoint timePoint) {
        if (timePoint == null) {
            return null;
        }
        return timePoint.asMillis();    
    }

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        if (serialized == null) {
            throw new TransformationException("Received nothing to deserialize");
        }
        JSONObject json = (JSONObject) serialized;
        UUID uuid = UUID.fromString((String) json.get(Fields.UUID.name()));
        String fileName = (String) json.get(Fields.FILE_NAME.name());
        String trackName = (String) json.get(Fields.TRACK_NAME.name());
        TimePoint uploaded = loadTimePoint(json.get(Fields.UPLOADED_MILLIS.name()));
        return new TrackFileImportDeviceIdentifierImpl(uuid, stringRepresentation, fileName, trackName, uploaded);
    }

    @Override
    public Util.Pair<String, JSONObject> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        TrackFileImportDeviceIdentifier id = TrackFileImportDeviceIdentifierImpl.cast(deviceIdentifier);
        JSONObject result = new JSONObject();
        result.put(Fields.UUID.name(), id.getId().toString());
        result.put(Fields.FILE_NAME.name(), id.getFileName());
        result.put(Fields.TRACK_NAME.name(), id.getTrackName());
        result.put(Fields.UPLOADED_MILLIS.name(), storeTimePoint(id.getUploadedAt()));
        return new Util.Pair<String ,JSONObject>(TrackFileImportDeviceIdentifier.TYPE, result);
    }

}
