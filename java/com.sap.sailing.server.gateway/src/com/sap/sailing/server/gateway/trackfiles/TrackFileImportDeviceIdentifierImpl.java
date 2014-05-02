package com.sap.sailing.server.gateway.trackfiles;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;

public class TrackFileImportDeviceIdentifierImpl implements TrackFileImportDeviceIdentifier {    
    private static final long serialVersionUID = 552465264341485161L;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final UUID id;
    private final String fileName;
    private final String trackName;
    private final TimePoint timePoint;
    
    public TrackFileImportDeviceIdentifierImpl(String fileName, String trackName) {
        this(UUID.randomUUID(), fileName, trackName, MillisecondsTimePoint.now());
    }
    
    public TrackFileImportDeviceIdentifierImpl(UUID id, String fileName, String trackName, TimePoint timePoint) {
        this.id = id;
        this.fileName = fileName;
        this.trackName = trackName;
        this.timePoint = timePoint;
    }

    @Override
    public String getIdentifierType() {
        return TYPE;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getStringRepresentation() {
        String dateString = timePoint == null ? "" : dateFormat.format(timePoint.asDate());
        return id.toString() + " - " + fileName + "(" + dateString + ") - " + trackName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrackFileImportDeviceIdentifier) {
            return id.equals(((TrackFileImportDeviceIdentifier) obj).getId());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getTrackName() {
        return trackName;
    }

    @Override
    public TimePoint getUploadedAt() {
        return timePoint;
    }
    
    public static TrackFileImportDeviceIdentifier cast(DeviceIdentifier deviceIdentifier) throws TransformationException {
        if (! (deviceIdentifier instanceof TrackFileImportDeviceIdentifier)) {
            throw new TransformationException("Expected TrackFileImportDeviceIdentifier, but got " + deviceIdentifier.getClass());
        }
        return (TrackFileImportDeviceIdentifier) deviceIdentifier;
    }
}
