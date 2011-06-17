package com.sap.sailing.domain.tracking.impl;

import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.RaceChangeListener;

public class DynamicTrackImpl<ItemType, FixType extends GPSFix> extends
        GPSFixTrackImpl<ItemType, FixType> implements DynamicTrack<ItemType, FixType> {
    private final Set<RaceChangeListener<ItemType>> listeners;
    
    public DynamicTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        super(trackedItem, millisecondsOverWhichToAverage);
        this.listeners = new HashSet<RaceChangeListener<ItemType>>();
    }

    @Override
    public void addGPSFix(FixType gpsFix) {
        getInternalFixes().add(gpsFix);
        for (RaceChangeListener<ItemType> listener : listeners) {
            listener.gpsFixReceived(gpsFix, getTrackedItem());
        }
    }

    @Override
    public void addListener(RaceChangeListener<ItemType> listener) {
        listeners.add(listener);
    }

    @Override
    public void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage) {
        long oldMillis = getMillisecondsOverWhichToAverage();
        super.setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverage);
        for (RaceChangeListener<ItemType> listener : listeners) {
            listener.speedAveragingChanged(oldMillis, millisecondsOverWhichToAverage);
        }
    }
    
}
