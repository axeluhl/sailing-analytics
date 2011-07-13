package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
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
public interface GPSFixTrack<ItemType, FixType extends GPSFix> extends Track<FixType> {
    ItemType getTrackedItem();

    /**
     * Computes the distance traveled between the {@link #getEstimatedPosition(TimePoint, boolean) estimated positions}
     * at <code>from</code> and <code>to</code>.
     */
    Distance getDistanceTraveled(TimePoint from, TimePoint to);

    /**
     * If the time point lies before the first fix recorded by this track, the first fix is returned, or
     * <code>null</code> if no fix at all exists yet. If a time point between two fixes of this track is chosen, the
     * path between the two fixes is interpolated along a great circle. The speed is estimated by using the time points
     * of the two fixes between which <code>timePoint</code> lies. If a time point after the last fix recorded so far is
     * provided and <code>extrapolate</code> is <code>true</code>, the {@link SpeedWithBearing} at the last point is
     * estimated, either from looking at the last two fixes in the track or, if only one fix exists, by using that
     * single fix's speed information if present (must be a {@link GPSFixMoving} for that). If <code>extrapolate</code>
     * is <code>false</code> and the <code>timePoint</code> is after the latest recorded fix, the latest recorded fix is
     * returned. If extrapolation is requested but not enough information is present to perform an estimation,
     * <code>null</code> is returned.
     * 
     * @param extrapolate
     *            only if <code>true</code> will the value for <code>timePoint</code> be computed by extrapolating
     *            beyond the time extent of this track; otherwise, the value closest to <code>timePoint</code> will be
     *            used instead.
     */
    Position getEstimatedPosition(TimePoint timePoint, boolean extrapolate);
    
    Speed getMaximumSpeedOverGround(TimePoint from, TimePoint to);

    /**
     * Using an averaging / smoothening algorithm, computes the estimated speed determined
     * by the GPS fixes in this track at time point <code>at</code>.
     */
    SpeedWithBearing getEstimatedSpeed(TimePoint at);

    long getMillisecondsOverWhichToAverageSpeed();

}
