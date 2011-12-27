package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.RaceChangeListener;

public class DynamicTrackImpl<ItemType, FixType extends GPSFix> extends
        GPSFixTrackImpl<ItemType, FixType> implements DynamicGPSFixTrack<ItemType, FixType> {
    public DynamicTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        super(trackedItem, millisecondsOverWhichToAverage);
    }

    public DynamicTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage, Speed maxSpeedForSmoothening) {
        super(trackedItem, millisecondsOverWhichToAverage, maxSpeedForSmoothening);
    }

    @Override
    public void addGPSFix(FixType gpsFix) {
        synchronized (this) {
            getInternalRawFixes().add(gpsFix);
            invalidateValidityCaches(gpsFix);
        }
        Iterable<RaceChangeListener<ItemType>> listeners = getListeners();
        synchronized (listeners) {
            for (RaceChangeListener<ItemType> listener : listeners) {
                listener.gpsFixReceived(gpsFix, getTrackedItem());
            }
        }
    }

    @Override
    public void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage) {
        long oldMillis = getMillisecondsOverWhichToAverage();
        super.setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverage);
        Iterable<RaceChangeListener<ItemType>> listeners = getListeners();
        synchronized (listeners) {
            for (RaceChangeListener<ItemType> listener : listeners) {
                listener.speedAveragingChanged(oldMillis, millisecondsOverWhichToAverage);
            }
        }
    }
    
}
