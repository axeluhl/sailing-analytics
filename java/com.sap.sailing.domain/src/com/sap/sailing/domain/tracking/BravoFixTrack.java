package com.sap.sailing.domain.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.common.tracking.BravoFix;
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
    Double getRideHeight(TimePoint timePoint);
    
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
    Double getAverageRideHeight(TimePoint from, TimePoint to);
}
