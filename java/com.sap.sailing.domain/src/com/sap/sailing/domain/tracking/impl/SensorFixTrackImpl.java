package com.sap.sailing.domain.tracking.impl;

import java.io.Serializable;
import java.util.function.Consumer;

import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.SensorFixTrack;
import com.sap.sailing.domain.tracking.SensorFixTrackListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.WithID;

/**
 * Implementation of {@link SensorFixTrack} and {@link DynamicSensorFixTrack}.
 *
 * @param <ItemType> the type of item this track is mapped to
 * @param <FixT> the type of fix this track holds
 */
public class SensorFixTrackImpl<ItemType extends WithID & Serializable, FixT extends SensorFix> extends
        DynamicTrackImpl<FixT> implements DynamicSensorFixTrack<ItemType, FixT> {

    private static final long serialVersionUID = 6383421895429843002L;
    
    private final Iterable<String> valueNames;
    private final ItemType trackedItem;
    private final String trackName;
    private final TrackListenerCollection<ItemType, FixT, SensorFixTrackListener<ItemType, FixT>> listeners;

    /**
     * @param trackedItem the item this track is mapped to
     * @param trackName the name of the track by which it can be obtained from the {@link TrackedRace}.
     * @param valueNames the name of the values that can be obtained by fixes contained in the track
     * @param nameForReadWriteLock the name to use for the lock object that is used internally
     */
    public SensorFixTrackImpl(ItemType trackedItem, String trackName, Iterable<String> valueNames,
            String nameForReadWriteLock) {
        super(nameForReadWriteLock);
        this.trackedItem = trackedItem;
        this.trackName = trackName;
        this.valueNames = valueNames;
        this.listeners = new TrackListenerCollection<>();
    }
    
    @Override
    public boolean add(FixT fix, boolean replace) {
        final boolean result;
        lockForWrite();
        try {
            final boolean firstFixInTrack = getRawFixes().isEmpty();
            result = addWithoutLocking(fix, replace);
            this.notifyListeners((listener) -> listener.fixReceived(fix, trackedItem, trackName, firstFixInTrack));
        } finally {
            unlockAfterWrite();
        }
        return result;
    }
    
    protected void notifyListeners(Consumer<SensorFixTrackListener<ItemType, FixT>> notification) {
        listeners.getListeners().forEach(notification);
    }

    @Override
    public Iterable<String> getValueNames() {
        return valueNames;
    }

    @Override
    public void addListener(SensorFixTrackListener<ItemType, FixT> listener) {
        this.listeners.addListener(listener);
    }
    
    @Override
    public void removeListener(SensorFixTrackListener<ItemType, FixT> listener) {
        this.listeners.removeListener(listener);
    }

    @Override
    public ItemType getTrackedItem() {
        return trackedItem;
    }

    @Override
    public String getTrackName() {
        return trackName;
    }

}