package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.tracking.BravoExtendedFix;
import com.sap.sailing.domain.common.tracking.BravoFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.WithID;

/**
 * Specific {@link SensorFixTrack} for {@link BravoFix}es.
 *
 * @param <ItemType> the type of item mapped by this track
 */
public interface BravoFixTrack<ItemType extends WithID & Serializable> extends SensorFixTrack<ItemType, BravoFix> {
    public static final String TRACK_NAME = "BravoFixTrack";
    
    /**
     * Calculates the ride height for a specific {@link TimePoint}
     * 
     * @param timePoint
     *            the {@link TimePoint} to get the ride height for. Must not be <code>null</code>.
     * @return the ride height for the given {@link TimePoint} or <code>null</code> if the {@link TimePoint} is not in the range
     *         where we have fixes for.
     */
    Distance getRideHeight(TimePoint timePoint);

    /**
     * Calculates the heel for a specific {@link TimePoint}
     * 
     * @param timePoint
     * @return
     */
    Bearing getHeel(TimePoint timePoint);

    /**
     * Calculates the pitch for a specific {@link TimePoint}
     * 
     * @param timePoint
     * @return
     */
    Bearing getPitch(TimePoint timePoint);

    /**
     * Tells if the competitor to which this track belongs is considered to be foiling at the time point
     * given. This depends on the {@link #getRideHeight} and a threshold above which a boat is considered
     * to be foiling.
     */
    boolean isFoiling(TimePoint timePoint);
    
    /**
     * Calculates the average ride height for the given time range.
     * 
     * @param from
     *            the lower bound of the time range to get the ride height for. Must not be <code>null</code>.
     * @param to
     *            the upper bound of the time range to get the ride height for. Must not be <code>null</code>.
     * @return the average ride height for the given time range or <code>null</code> if there are no fixes in the given
     *         time range.
     */
    Distance getAverageRideHeight(TimePoint from, TimePoint to);
    
    /**
     * The time spent foiling during the time range provided
     */
    Duration getTimeSpentFoiling(TimePoint from, TimePoint to);

    /**
     * The distance sailed foiling during the time range provided
     */
    Distance getDistanceSpentFoiling(GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack, TimePoint from, TimePoint to);
    
    /**
     * Returns {@code true}, if the track contains {@link BravoExtendedFix} instances instead of simple {@link BravoFix BravoFixes}.
     * It is not guaranteed that the track exclusively contains {@link BravoExtendedFix BravoExtendedFixes} if this is {@code true}.
     * It is up to the operator to ensure, that either normal Bravo data or the extended data is imported, but it isn't enforced.
     * If both types of fixes are contained, data may seem to have "gaps" because single fixes returned do not provide the extended data.
     */
    boolean hasExtendedFixes();
    
    Bearing getDbRakePortIfAvailable(TimePoint timePoint);
    Bearing getDbRakeStbdIfAvailable(TimePoint timePoint);
    Bearing getRudderRakePortIfAvailable(TimePoint timePoint);
    Bearing getRudderRakeStbdIfAvailable(TimePoint timePoint);
    Bearing getMastRotationIfAvailable(TimePoint timePoint);
}
