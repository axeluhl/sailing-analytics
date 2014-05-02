package com.sap.sailing.server.gateway.trackfiles;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.tracking.DeviceIdentifier;

public class TrackFileImportDeviceIdentifierImpl implements TrackFileImportDeviceIdentifier {    
    private static final long serialVersionUID = 552465264341485161L;
    private final UUID id;
    private final String fileName;
    private final String trackName;
    private final TimePoint timePoint;
    private final TimeRange timeRange;
    private final long numberOfFixes;
    
    private static final Map<UUID, TrackFileImportDeviceIdentifier> cache = new ConcurrentHashMap<>();
    
    public static TrackFileImportDeviceIdentifier getOrCreate(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }
        return new TrackFileImportDeviceIdentifierImpl(uuid, null, null, null, null, -1);
    }
    
    public TrackFileImportDeviceIdentifierImpl(String fileName, String trackName, TimeRange timeRange, long numberOfFixes) {
        this(UUID.randomUUID(), fileName, trackName, MillisecondsTimePoint.now(), timeRange, numberOfFixes);
    }
    
    public TrackFileImportDeviceIdentifierImpl(UUID id, String fileName, String trackName, TimePoint timePoint, TimeRange timeRange, long numberOfFixes) {
        this.id = id;
        this.fileName = fileName;
        this.trackName = trackName;
        this.timePoint = timePoint;
        this.timeRange = timeRange;
        this.numberOfFixes = numberOfFixes;
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
        return String.format("%s: %s(%s, %s fixes)\n  @%s(uploaded %s)",
                id.toString(), trackName, timeRange, numberOfFixes, fileName, timePoint);
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

    @Override
    public TimeRange getFixesTimeRange() {
        return timeRange;
    }

    @Override
    public long getNumberOfFixes() {
        return numberOfFixes;
    }
}
