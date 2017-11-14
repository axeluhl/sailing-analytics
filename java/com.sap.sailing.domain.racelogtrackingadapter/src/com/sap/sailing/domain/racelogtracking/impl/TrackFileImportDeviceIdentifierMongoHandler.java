package com.sap.sailing.domain.racelogtracking.impl;

import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.persistence.racelog.tracking.DeviceIdentifierMongoHandler;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class TrackFileImportDeviceIdentifierMongoHandler implements DeviceIdentifierMongoHandler {
    private static enum Fields {UUID, FILE_NAME, TRACK_NAME, UPLOADED_MILLIS};

    @Override
    public DeviceIdentifier deserialize(Object serialized, String type, String stringRepresentation)
            throws TransformationException {
        DBObject dbObject = (DBObject) serialized;
        UUID uuid = UUID.fromString((String) dbObject.get(Fields.UUID.name()));
        String fileName = (String) dbObject.get(Fields.FILE_NAME.name());
        String trackName = (String) dbObject.get(Fields.TRACK_NAME.name());
        TimePoint uploaded = TrackFileImportDeviceIdentifierJsonHandler.
                loadTimePoint(dbObject.get(Fields.UPLOADED_MILLIS.name()));
        return new TrackFileImportDeviceIdentifierImpl(uuid, stringRepresentation, fileName, trackName, uploaded);
    }

    @Override
    public Util.Pair<String ,DBObject> serialize(DeviceIdentifier deviceIdentifier) throws TransformationException {
        TrackFileImportDeviceIdentifier id = TrackFileImportDeviceIdentifierImpl.cast(deviceIdentifier);
        DBObject result = new BasicDBObject();
        result.put(Fields.UUID.name(), id.getId().toString());
        result.put(Fields.FILE_NAME.name(), id.getFileName());
        result.put(Fields.TRACK_NAME.name(), id.getTrackName());
        result.put(Fields.UPLOADED_MILLIS.name(), TrackFileImportDeviceIdentifierJsonHandler.
                storeTimePoint(id.getUploadedAt()));
        return new Util.Pair<String ,DBObject>(TrackFileImportDeviceIdentifier.TYPE, result);
    }

}
