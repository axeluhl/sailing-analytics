package com.sap.sailing.domain.tracking.impl;

import java.util.OptionalDouble;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicBravoFixTrack;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

public class BravoFixTrackImpl extends SensorFixTrackImpl<BravoFix> implements DynamicBravoFixTrack {
    private static final long serialVersionUID = 3045856366552398911L;

    public BravoFixTrackImpl(WithID trackedItem) {
        super(BravoSensorDataMetadata.INSTANCE.getColumns(), BravoFixTrack.TRACK_NAME + " for " + trackedItem);
    }

    @Override
    public Double getRideHeight(TimePoint timePoint) {
        BravoFix fixAfter = getFirstFixAtOrAfter(timePoint);
        if(fixAfter != null && fixAfter.getTimePoint().compareTo(timePoint) == 0) {
            // exact match of timepoint -> no interpolation necessary
            return fixAfter.getRideHeight();
        }
        BravoFix fixBefore = getLastFixAtOrBefore(timePoint);
        if(fixBefore != null && fixBefore.getTimePoint().compareTo(timePoint) == 0) {
            // exact match of timepoint -> no interpolation necessary
            return fixBefore.getRideHeight();
        }
        if(fixAfter == null || fixBefore == null) {
            // the fix is out of the TimeRange where we have fixes
            return null;
        }
        // TODO interpolate if necessary
        return fixBefore.getRideHeight();
    }
    
    @Override
    public Double getAverageRideHeight(TimePoint from, TimePoint to) {
        try {
            lockForRead();
            Spliterator<BravoFix> fixes = getFixes(from, true, to, true).spliterator();
            OptionalDouble average = StreamSupport.stream(fixes, false).mapToDouble(BravoFix::getRideHeight).average();
            if (average.isPresent()) {
                return average.getAsDouble();
            }
        } finally {
            unlockAfterRead();
        }
        return null;
    }
}
