package com.sap.sailing.domain.tracking.impl;

import java.io.Serializable;
import java.util.OptionalDouble;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.common.sensordata.BravoSensorDataMetadata;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicBravoFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

/**
 * Specific {@link SensorFixTrackImpl} used for {@link BravoFix}es.
 *
 * @param <ItemType> the type of item this track is mapped to
 */
public class BravoFixTrackImpl<ItemType extends WithID & Serializable> extends SensorFixTrackImpl<ItemType, BravoFix>
        implements DynamicBravoFixTrack<ItemType> {
    private static final long serialVersionUID = 5517848726456424386L;

    /**
     * @param trackedItem
     *            the item this track is mapped to
     * @param trackName
     *            the name of the track by which it can be obtained from the {@link TrackedRace}.
     */
    public BravoFixTrackImpl(ItemType trackedItem, String trackName) {
        super(trackedItem, trackName, BravoSensorDataMetadata.INSTANCE.getColumns(), 
                BravoFixTrack.TRACK_NAME + " for " + trackedItem);
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
