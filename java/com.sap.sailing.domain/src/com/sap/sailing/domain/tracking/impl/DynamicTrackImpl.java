package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSTrackListener;

public class DynamicTrackImpl<ItemType, FixType extends GPSFix> extends
        GPSFixTrackImpl<ItemType, FixType> implements DynamicGPSFixTrack<ItemType, FixType> {
    private static final long serialVersionUID = 917778209274148097L;

    public DynamicTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        super(trackedItem, millisecondsOverWhichToAverage);
    }

    public DynamicTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage, Speed maxSpeedForSmoothening) {
        super(trackedItem, millisecondsOverWhichToAverage, maxSpeedForSmoothening);
    }

    @Override
    public void addGPSFix(FixType gpsFix) {
        lockForWrite();
        try {
            getInternalRawFixes().add(gpsFix);
            invalidateValidityAndDistanceCaches(gpsFix);
        } finally {
            unlockAfterWrite();
        }
        for (GPSTrackListener<ItemType, FixType> listener : getListeners()) {
            listener.gpsFixReceived(gpsFix, getTrackedItem());
        }
    }

    @Override
    public void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage) {
        long oldMillis = getMillisecondsOverWhichToAverage();
        super.setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverage);
        for (GPSTrackListener<ItemType, FixType> listener : getListeners()) {
            listener.speedAveragingChanged(oldMillis, millisecondsOverWhichToAverage);
        }
    }
    
}
