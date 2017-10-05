package com.sap.sailing.domain.tracking.impl;

import java.io.Serializable;
import java.util.function.Function;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.tracking.BravoExtendedFix;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sailing.domain.tracking.DynamicBravoFixTrack;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

/**
 * Specific {@link SensorFixTrackImpl} used for {@link BravoFix}es.
 *
 * @param <ItemType> the type of item this track is mapped to
 */
public class BravoFixTrackImpl<ItemType extends WithID & Serializable> extends SensorFixTrackImpl<ItemType, BravoFix>
        implements DynamicBravoFixTrack<ItemType> {
    private static final long serialVersionUID = 460944392510182976L;
    
    private final boolean hasExtendedFixes;

    /**
     * @param trackedItem
     *            the item this track is mapped to
     * @param trackName
     *            the name of the track by which it can be obtained from the {@link TrackedRace}.
     */
    public BravoFixTrackImpl(ItemType trackedItem, String trackName, boolean hasExtendedFixes) {
        super(trackedItem, trackName, BravoFixTrack.TRACK_NAME + " for " + trackedItem);
        this.hasExtendedFixes = hasExtendedFixes;
    }

    @Override
    public Distance getRideHeight(TimePoint timePoint) {
        BravoFix fixAfter = getFirstFixAtOrAfter(timePoint);
        if (fixAfter != null && fixAfter.getTimePoint().compareTo(timePoint) == 0) {
            // exact match of timepoint -> no interpolation necessary
            return fixAfter.getRideHeight();
        }
        BravoFix fixBefore = getLastFixAtOrBefore(timePoint);
        if (fixBefore != null && fixBefore.getTimePoint().compareTo(timePoint) == 0) {
            // exact match of timepoint -> no interpolation necessary
            return fixBefore.getRideHeight();
        }
        if (fixAfter == null || fixBefore == null) {
            // the fix is out of the TimeRange where we have fixes
            return null;
        }
        // TODO interpolate if necessary
        return fixBefore.getRideHeight();
    }

    @Override
    public Bearing getHeel(TimePoint timePoint) {
        BravoFix fixAfter = getFirstFixAtOrAfter(timePoint);
        if (fixAfter != null && fixAfter.getTimePoint().compareTo(timePoint) == 0) {
            // exact match of timepoint -> no interpolation necessary
            return fixAfter.getHeel();
        }
        BravoFix fixBefore = getLastFixAtOrBefore(timePoint);
        if (fixBefore != null && fixBefore.getTimePoint().compareTo(timePoint) == 0) {
            // exact match of timepoint -> no interpolation necessary
            return fixBefore.getHeel();
        }
        if (fixAfter == null || fixBefore == null) {
            // the fix is out of the TimeRange where we have fixes
            return null;
        }
        // TODO interpolate if necessary
        return fixBefore.getHeel();
    }

    @Override
    public Bearing getPitch(TimePoint timePoint) {
        BravoFix fixAfter = getFirstFixAtOrAfter(timePoint);
        if (fixAfter != null && fixAfter.getTimePoint().compareTo(timePoint) == 0) {
            // exact match of timepoint -> no interpolation necessary
            return fixAfter.getPitch();
        }
        BravoFix fixBefore = getLastFixAtOrBefore(timePoint);
        if (fixBefore != null && fixBefore.getTimePoint().compareTo(timePoint) == 0) {
            // exact match of timepoint -> no interpolation necessary
            return fixBefore.getPitch();
        }
        if (fixAfter == null || fixBefore == null) {
            // the fix is out of the TimeRange where we have fixes
            return null;
        }
        // TODO interpolate if necessary
        return fixBefore.getPitch();
    }
    
    @Override
    public boolean isFoiling(TimePoint timePoint) {
        final Distance rideHeight = getRideHeight(timePoint);
        return rideHeight != null && rideHeight.compareTo(BravoFix.MIN_FOILING_HEIGHT_THRESHOLD) >= 0;
    }

    @Override
    public Distance getAverageRideHeight(TimePoint from, TimePoint to) {
        final Distance result;
        lockForRead();
        try {
            Distance sum = Distance.NULL;
            int count = 0;
            for (final BravoFix fix : getFixes(from, true, to, true)) {
                sum = sum.add(fix.getRideHeight());
                count++;
            }
            if (count > 0) {
                result = sum.scale(1./count);
            } else {
                result = null;
            }
        } finally {
            unlockAfterRead();
        }
        return result;
    }
    
    private boolean isFoiling(BravoFix fix) {
        return fix.isFoiling();
    }

    @Override
    public Duration getTimeSpentFoiling(TimePoint from, TimePoint to) {
        Duration result = Duration.NULL;
        lockForRead();
        try {
            TimePoint last = from;
            boolean isFoiling = false;
            for (final BravoFix fix : getFixes(from, true, to, true)) {
                final boolean fixFoils = isFoiling(fix);
                if (isFoiling && fixFoils) {
                    result = result.plus(last.until(fix.getTimePoint()));
                }
                last = fix.getTimePoint();
                isFoiling = fixFoils;
            }
        } finally {
            unlockAfterRead();
        }
        return result;
    }

    @Override
    public Distance getDistanceSpentFoiling(GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack, TimePoint from, TimePoint to) {
        Distance result = Distance.NULL;
        lockForRead();
        try {
            TimePoint last = from;
            boolean isFoiling = false;
            for (final BravoFix fix : getFixes(from, true, to, true)) {
                final boolean fixFoils = isFoiling(fix);
                if (isFoiling && fixFoils) {
                    result = result.add(gpsFixTrack.getDistanceTraveled(last, fix.getTimePoint()));
                }
                last = fix.getTimePoint();
                isFoiling = fixFoils;
            }
        } finally {
            unlockAfterRead();
        }
        return result;
    }

    @Override
    public boolean hasExtendedFixes() {
        return hasExtendedFixes;
    }
    
    /**
     * Generic implementation to get values from extended fixes. The implementation ensured that in case of simple
     * {@link BravoFix} instances, just {@code null} is returned. If valid {@link BravoExtendedFix BravoExtendedFixes}
     * are found, the provided getter is used to extract the value from the identified fix.
     * 
     * In case of a mix of {@link BravoFix} and {@link BravoExtendedFix} instances, this method may return null values
     * for specific {@link TimePoint TimePoints}.
     */
    private Bearing getBearingValueFromExtendedFix(final TimePoint timePoint, final Function<BravoExtendedFix, Bearing> getter) {
        if (!hasExtendedFixes) {
            return null;
        }
        final BravoExtendedFix fixAfter = getFirstFixAtOrAfterIfExtended(timePoint);
        if (fixAfter != null && fixAfter.getTimePoint().compareTo(timePoint) == 0) {
            // exact match of timepoint -> no interpolation necessary
            return getter.apply(fixAfter);
        }
        final BravoExtendedFix fixBefore = getLastFixAtOrBeforeIfExtended(timePoint);
        if (fixBefore != null && fixBefore.getTimePoint().compareTo(timePoint) == 0) {
            // exact match of timepoint -> no interpolation necessary
            return getter.apply(fixBefore);
        }
        if (fixAfter == null || fixBefore == null) {
            // the fix is out of the TimeRange where we have fixes
            return null;
        }
        // TODO interpolate if necessary
        return getter.apply(fixBefore);
    }
    
    public BravoExtendedFix getFirstFixAtOrAfterIfExtended(TimePoint timePoint) {
        BravoFix fix = getFirstFixAtOrAfter(timePoint);
        return fix instanceof BravoExtendedFix ? (BravoExtendedFix) fix : null;
    }
    
    public BravoExtendedFix getLastFixAtOrBeforeIfExtended(TimePoint timePoint) {
        BravoFix fix = getLastFixAtOrBefore(timePoint);
        return fix instanceof BravoExtendedFix ? (BravoExtendedFix) fix : null;
    }

    @Override
    public Bearing getDbRakePortIfAvailable(TimePoint timePoint) {
        return getBearingValueFromExtendedFix(timePoint, BravoExtendedFix::getDbRakePort);
    }

    @Override
    public Bearing getDbRakeStbdIfAvailable(TimePoint timePoint) {
        return getBearingValueFromExtendedFix(timePoint, BravoExtendedFix::getDbRakeStbd);
    }

    @Override
    public Bearing getRudderRakePortIfAvailable(TimePoint timePoint) {
        return getBearingValueFromExtendedFix(timePoint, BravoExtendedFix::getRudderRakePort);
    }

    @Override
    public Bearing getRudderRakeStbdIfAvailable(TimePoint timePoint) {
        return getBearingValueFromExtendedFix(timePoint, BravoExtendedFix::getRudderRakeStbd);
    }

    @Override
    public Bearing getMastRotationIfAvailable(TimePoint timePoint) {
        return getBearingValueFromExtendedFix(timePoint, BravoExtendedFix::getMastRotation);
    }
}