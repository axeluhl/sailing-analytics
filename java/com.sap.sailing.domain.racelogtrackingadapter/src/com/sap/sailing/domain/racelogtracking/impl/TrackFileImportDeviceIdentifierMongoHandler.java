package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.TrackFileImportDeviceIdentifierImpl;

public class TrackFileImportDeviceIdentifierMongoHandler implements DeviceIdentifierMongoHandler {
    private static enum Fields {UUID, FILE_NAME, TRACK_NAME, DATE_AS_MILLIS};

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        DBObject dbObject = (DBObject) serialized;
        UUID uuid = UUID.fromString((String) dbObject.get(Fields.UUID.name()));
        String fileName = (String) dbObject.get(Fields.FILE_NAME.name());
        String trackName = (String) dbObject.get(Fields.TRACK_NAME.name());
        Long dateAsMillis = (Long) dbObject.get(Fields.DATE_AS_MILLIS.name());
        TimePoint timePoint = dateAsMillis == null ? null : new MillisecondsTimePoint(dateAsMillis);
        return new TrackFileImportDeviceIdentifierImpl(uuid, fileName, trackName, timePoint);
    }

    @Override
    public Pair<DBObject, String> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        TrackFileImportDeviceIdentifier id = TrackFileImportDeviceIdentifierImpl.cast(deviceIdentifier);
        DBObject result = new BasicDBObject();
        result.put(Fields.UUID.name(), id.getId());
        result.put(Fields.FILE_NAME.name(), id.getFileName());
        result.put(Fields.TRACK_NAME.name(), id.getTrackName());
        result.put(Fields.DATE_AS_MILLIS.name(), id.getUploadedAt() == null ? null : id.getUploadedAt().asMillis());
        return new Pair<DBObject, String>(result, TrackFileImportDeviceIdentifier.TYPE);
    }

}
