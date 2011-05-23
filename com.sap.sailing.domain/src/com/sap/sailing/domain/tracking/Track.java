package com.sap.sailing.domain.tracking;

import java.util.Iterator;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;

/**
 * A track records the {@link GPSFix}es received for an object of type
 * <code>ItemType</code>. It allows clients to ask for a position at any given
 * {@link TimePoint} and interpolates the fixed positions to obtain an estimate
 * of the position at the time requested.
 * 
 * @author Axel Uhl (d043530)
 * 
 * @param <ItemType>
 */
public interface Track<ItemType, FixType extends GPSFix> {
    ItemType getTrackedItem();

    Iterable<FixType> getFixes();

    FixType getLastFixAtOrBefore(TimePoint timePoint);

    FixType getFirstFixAtOrAfter(TimePoint timePoint);

    FixType getLastFixBefore(TimePoint timePoint);

    FixType getFirstFixAfter(TimePoint timePoint);
    
    Distance getDistanceTraveled(TimePoint from, TimePoint to);

    /**
     * If the time point lies before the first fix recorded by this track,
     * the first fix is returned, or <code>null</code> if no fix at all exists yet.
     * If a time point between two fixes of this track is chosen, the path between
     * the two fixes is interpolated along a great circle. The speed is estimated
     * by using the time points of the two fixes between which <code>timePoint</code>
     * lies. If a time point after the last fix recorded so far is provided,
     * the {@link SpeedWithBearing} at the last point is estimated, either from looking
     * at the last two fixes in the track or, if only one fix exists, by
     * using that single fix's speed information if present (must be a
     * {@link GPSFixMoving} for that). Otherwise, if not enough information
     * is present to perform an estimation, <code>null</code> is returned.
     */
    Position getEstimatedPosition(TimePoint timePoint);

    /**
     * Returns an iterator starting at the first fix after <code>startingAt</code> (or
     * "at or after" in case <code>inclusive</code> is <code>true</code>).
     */
    Iterator<FixType> getFixes(TimePoint startingAt, boolean inclusive);
}
