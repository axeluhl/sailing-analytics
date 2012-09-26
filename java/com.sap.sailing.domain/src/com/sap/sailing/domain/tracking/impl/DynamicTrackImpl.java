package com.sap.sailing.domain.tracking.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSTrackListener;

public class DynamicTrackImpl<ItemType, FixType extends GPSFix> extends
        GPSFixTrackImpl<ItemType, FixType> implements DynamicGPSFixTrack<ItemType, FixType> {
    private static final long serialVersionUID = 917778209274148097L;
    private static final Logger logger = Logger.getLogger(DynamicTrackImpl.class.getName());

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
        if (logger.isLoggable(Level.FINEST)) {
            FixType last;
            logger.finest("GPS fix "+gpsFix+" for "+getTrackedItem()+", isValid="+gpsFix.isValid()+", time/distance/speed from last: "+
                    ((last=getInternalRawFixes().lower(gpsFix))==null
                    ? "null"
                    : (gpsFix.getTimePoint().asMillis()-last.getTimePoint().asMillis())+"ms/"+
                      gpsFix.getPosition().getDistance(last.getPosition())) + "/"+
                      gpsFix.getPosition().getDistance(last.getPosition()).inTime(gpsFix.getTimePoint().asMillis()-last.getTimePoint().asMillis()));
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
