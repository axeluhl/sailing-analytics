package com.sap.sailing.domain.tracking.impl;

import java.io.Serializable;

import com.sap.sailing.domain.common.tracking.SensorFix;
import com.sap.sailing.domain.tracking.DynamicSensorFixTrack;
import com.sap.sailing.domain.tracking.SensorFixTrackListener;
import com.sap.sse.common.WithID;

public class SensorFixTrackImpl<ItemType extends WithID & Serializable, FixT extends SensorFix> extends
        DynamicTrackImpl<FixT> implements DynamicSensorFixTrack<ItemType, FixT> {

    private static final long serialVersionUID = 6383421895429843002L;
    
    private final Iterable<String> valueNames;
    private final ItemType trackedItem;
    private final String trackName;
    private final TrackListenerCollection<ItemType, FixT, SensorFixTrackListener<ItemType, FixT>> listeners;

    public SensorFixTrackImpl(ItemType trackedItem, String trackName, Iterable<String> valueNames,
            String nameForReadWriteLock) {
        super(nameForReadWriteLock);
        this.trackedItem = trackedItem;
        this.trackName = trackName;
        this.valueNames = valueNames;
        this.listeners = new TrackListenerCollection<>();
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