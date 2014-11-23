package com.sap.sailing.domain.trackfiles;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.abstractlog.race.tracking.DeviceIdentifier;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackFileImportDeviceIdentifierImpl implements TrackFileImportDeviceIdentifier {    
    private static final long serialVersionUID = 552465264341485161L;
    private final UUID id;
    private final String fileName;
    private final String trackName;
    private final TimePoint timePoint;
    
    private static final Map<UUID, TrackFileImportDeviceIdentifier> cache = new ConcurrentHashMap<>();
    
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
        this.id = id;
        this.fileName = fileName;
        this.trackName = trackName;
        this.timePoint = timePoint;
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
        return String.format("%s: %s\n  @%s(uploaded %s)",
                id.toString(), trackName, fileName, timePoint);
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
