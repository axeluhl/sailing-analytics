package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.impl.TimeRangeImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.server.gateway.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.server.gateway.trackfiles.TrackFileImportDeviceIdentifierImpl;

public class TrackFileImportDeviceIdentifierMongoHandler implements DeviceIdentifierMongoHandler {
    private static enum Fields {UUID, FILE_NAME, TRACK_NAME, UPLOADED_MILLIS, FROM_MILLIS, TO_MILLIS, NUM_FIXES};

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        DBObject dbObject = (DBObject) serialized;
        UUID uuid = UUID.fromString((String) dbObject.get(Fields.UUID.name()));
        String fileName = (String) dbObject.get(Fields.FILE_NAME.name());
        String trackName = (String) dbObject.get(Fields.TRACK_NAME.name());
        TimePoint uploaded = TrackFileImportDeviceIdentifierJsonHandler.
                loadTimePoint(dbObject.get(Fields.UPLOADED_MILLIS.name()));
        TimePoint from = TrackFileImportDeviceIdentifierJsonHandler.
                loadTimePoint(dbObject.get(Fields.FROM_MILLIS.name()));
        TimePoint to = TrackFileImportDeviceIdentifierJsonHandler.
                loadTimePoint(dbObject.get(Fields.TO_MILLIS.name()));
        TimeRange timeRange = from != null && to != null ? new TimeRangeImpl(from, to) : null;
        Long numberOfFixes = (Long) dbObject.get(Fields.NUM_FIXES.name());
        return new TrackFileImportDeviceIdentifierImpl(uuid, fileName, trackName, uploaded, timeRange,
                numberOfFixes == null ? -1 : numberOfFixes);
    }

    @Override
    public Pair<DBObject, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        TrackFileImportDeviceIdentifier id = TrackFileImportDeviceIdentifierImpl.cast(deviceIdentifier);
        DBObject result = new BasicDBObject();
        result.put(Fields.UUID.name(), id.getId().toString());
        result.put(Fields.FILE_NAME.name(), id.getFileName());
        result.put(Fields.TRACK_NAME.name(), id.getTrackName());
        result.put(Fields.UPLOADED_MILLIS.name(), TrackFileImportDeviceIdentifierJsonHandler.
                storeTimePoint(id.getUploadedAt()));
        result.put(Fields.FROM_MILLIS.name(), TrackFileImportDeviceIdentifierJsonHandler.
                storeTimePoint(id.getFixesTimeRange().from()));
        result.put(Fields.TO_MILLIS.name(), TrackFileImportDeviceIdentifierJsonHandler.
                storeTimePoint(id.getFixesTimeRange().to()));
        result.put(Fields.NUM_FIXES.name(), id.getNumberOfFixes());
        return new Pair<DBObject, String>(result, TrackFileImportDeviceIdentifier.TYPE);
    }

}
