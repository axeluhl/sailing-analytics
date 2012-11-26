package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.Weigher;

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
    static final long DEFAULT_MILLISECONDS_OVER_WHICH_TO_AVERAGE_SPEED = 10000; // makes for a 5s half-side interval

    /**
     * A listener is notified whenever a new fix is added to this track
     */
    void addListener(GPSTrackListener<ItemType, FixType> listener);
    
    void removeListener(GPSTrackListener<ItemType, FixType> listener);
    
    ItemType getTrackedItem();

    /**
     * Computes the distance traveled on the smoothened track between the
     * {@link #getEstimatedPosition(TimePoint, boolean) estimated positions} at <code>from</code> and <code>to</code>.
     */
    Distance getDistanceTraveled(TimePoint from, TimePoint to);

    /**
     * Computes the distance traveled on the raw, unsmoothened track between the
     * {@link #getEstimatedPosition(TimePoint, boolean) estimated positions} at <code>from</code> and <code>to</code>.
     * This includes all zig-zagging caused by imprecise GPS measurements.
     */
    Distance getRawDistanceTraveled(TimePoint from, TimePoint to);

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
    
    /**
     * Same as {@link #getEstimatedPosition(TimePoint, boolean)}, only that it works on the raw track
     * that has not been subject to smoothening.
     */
    Position getEstimatedRawPosition(TimePoint timePoint, boolean extrapolate);
    
    Pair<FixType, Speed> getMaximumSpeedOverGround(TimePoint from, TimePoint to);

    /**
     * Using an averaging / smoothening algorithm, computes the estimated speed determined
     * by the GPS fixes in this track at time point <code>at</code>.
     */
    SpeedWithBearing getEstimatedSpeed(TimePoint at);
    
    /**
     * Estimates the tracked item's speed/bearing, returning the result as a value with confidence. The confidences of
     * the individual fixes contributing to the estimation are computed using the <code>weigher</code>. The result's
     * confidence is the average confidence of the fixes used. While this is probably not a mathematically meaningful
     * confidence value, it helps in comparing confidences of different estimations relative to each other.
     */
    SpeedWithBearingWithConfidence<TimePoint> getEstimatedSpeed(TimePoint at, Weigher<TimePoint> weigher);

    /**
     * Same as {@link #getEstimatedSpeed(TimePoint, Weigher)}, but using the raw fixes.
     */
    SpeedWithBearingWithConfidence<TimePoint> getRawEstimatedSpeed(TimePoint at, Weigher<TimePoint> weigher);

    long getMillisecondsOverWhichToAverageSpeed();
    
    /**
     * If and only if the {@link #getFixesIterator(TimePoint, boolean) smoothened track} has a direction change of at
     * least <code>minimumDegreeDifference</code> degrees within {@link #getMillisecondsOverWhichToAverageSpeed()}
     * milliseconds around the <code>at</code> time point, this method returns <code>true</code>.
     */
    boolean hasDirectionChange(TimePoint at, double minimumDegreeDifference);

    SpeedWithBearing getRawEstimatedSpeed(TimePoint at);

    /**
     * Finds out which position estimation time interval has been affected by inserting <code>fix</code>.
     * 
     * @param fix
     *            assumed to already have been inserted into this track, but it's OK to pass a fix that's not in the
     *            track yet
     * 
     * @return if no fix before <code>fix</code> is found, the first component is <code>fix.getTimePoint()</code>. If no fix after
     *         <code>fix</code> is found, the second component is <code>fix.getTimePoint()</code>.
     */
    Pair<TimePoint, TimePoint> getEstimatedPositionTimePeriodAffectedBy(GPSFix fix);

}
