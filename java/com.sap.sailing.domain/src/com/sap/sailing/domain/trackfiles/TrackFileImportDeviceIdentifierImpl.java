package com.sap.sailing.domain.trackfiles;

import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackFileImportDeviceIdentifierImpl implements TrackFileImportDeviceIdentifier {    
    private static final long serialVersionUID = 552465264341485161L;
    private final UUID id;
    private final String fileName;
    private final String trackName;
    private final TimePoint timePoint;
    private final String stringRepresentation;
    
    private static final ConcurrentMap<UUID, TrackFileImportDeviceIdentifier> cache = new ConcurrentHashMap<>();
    
    public static TrackFileImportDeviceIdentifier getOrCreate(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }
        return new TrackFileImportDeviceIdentifierImpl(uuid, null, null, null);
    }
    
    public TrackFileImportDeviceIdentifierImpl(String fileName, String trackName) {
        this(UUID.randomUUID(), fileName, trackName, MillisecondsTimePoint.now());
    }
    
    public TrackFileImportDeviceIdentifierImpl(UUID id, String fileName, String trackName, TimePoint timePoint) {
        this(id, computeStringRepresentation(id, trackName, fileName, timePoint), fileName,
                trackName, timePoint);
        cache.put(id, this);
    }

    public TrackFileImportDeviceIdentifierImpl(UUID id, String stringRepresentation, String fileName, String trackName,
            TimePoint timePoint) {
        this.id = id;
        this.fileName = fileName;
        this.trackName = trackName;
        this.timePoint = timePoint;
        this.stringRepresentation = stringRepresentation;
        cache.put(id, this);
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
    public String toString() {
        return id.toString();
    }

    @Override
    public String getStringRepresentation() {
        return stringRepresentation;
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

    /**
     * Computes unique string representation for this device identifier. This string representaion should be human
     * readable.
     * 
     * old computed representation algorithm: String.format("%s: %s\n  @%s(uploaded %s)", id.toString(), trackName,
     * fileName, timePoint)
     * 
     * @param id
     * @param fileName
     * @param trackName
     * @param timePoint
     * @return
     */
    
    private static final String computeStringRepresentation(UUID id, String fileName, String trackName,
            TimePoint timePoint) {
        StringBuilder sb = new StringBuilder();
        sb.append(id.toString()).append(": ");
        sb.append(trackName);
        sb.append("\n  ");
        sb.append("@").append(fileName);
        sb.append("(uploaded ").append(DateTimeFormatter.ISO_INSTANT.format(timePoint.asDate().toInstant()))
                .append(")");
        return sb.toString();
    }
}
