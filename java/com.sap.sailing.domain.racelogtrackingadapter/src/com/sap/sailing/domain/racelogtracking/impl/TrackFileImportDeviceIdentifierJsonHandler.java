package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.TimeRangeImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.server.gateway.trackfiles.TrackFileImportDeviceIdentifierImpl;

public class TrackFileImportDeviceIdentifierJsonHandler implements DeviceIdentifierJsonHandler {
    private static enum Fields {UUID, FILE_NAME, TRACK_NAME, UPLOADED_MILLIS, FROM_MILLIS, TO_MILLIS, NUM_FIXES};
    
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
        JSONObject json = (JSONObject) serialized;
        UUID uuid = UUID.fromString((String) json.get(Fields.UUID.name()));
        String fileName = (String) json.get(Fields.FILE_NAME.name());
        String trackName = (String) json.get(Fields.TRACK_NAME.name());
        TimePoint uploaded = loadTimePoint(json.get(Fields.UPLOADED_MILLIS.name()));
        TimePoint from = loadTimePoint(json.get(Fields.FROM_MILLIS.name()));
        TimePoint to = loadTimePoint(json.get(Fields.TO_MILLIS.name()));
        TimeRange timeRange = from != null && to != null ? new TimeRangeImpl(from, to) : null;
        Long numberOfFixes = (Long) json.get(Fields.NUM_FIXES.name());
        return new TrackFileImportDeviceIdentifierImpl(uuid, fileName, trackName, uploaded, timeRange,
                numberOfFixes == null ? -1 : numberOfFixes);
    }

    @Override
    public Pair<String, JSONObject> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        TrackFileImportDeviceIdentifier id = TrackFileImportDeviceIdentifierImpl.cast(deviceIdentifier);
        JSONObject result = new JSONObject();
        result.put(Fields.UUID.name(), id.getId().toString());
        result.put(Fields.FILE_NAME.name(), id.getFileName());
        result.put(Fields.TRACK_NAME.name(), id.getTrackName());
        result.put(Fields.UPLOADED_MILLIS.name(), storeTimePoint(id.getUploadedAt()));
        result.put(Fields.FROM_MILLIS.name(), storeTimePoint(id.getFixesTimeRange().from()));
        result.put(Fields.TO_MILLIS.name(), storeTimePoint(id.getFixesTimeRange().to()));
        result.put(Fields.NUM_FIXES.name(), id.getNumberOfFixes());
        return new Pair<String ,JSONObject>(TrackFileImportDeviceIdentifier.TYPE, result);
    }

}
