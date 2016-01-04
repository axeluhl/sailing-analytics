package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.confidence.BearingWithConfidence;
import com.sap.sailing.domain.common.confidence.BearingWithConfidenceCluster;
import com.sap.sailing.domain.common.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.common.confidence.ConfidenceFactory;
import com.sap.sailing.domain.common.confidence.HasConfidence;
import com.sap.sailing.domain.common.confidence.Weigher;
import com.sap.sailing.domain.common.confidence.impl.BearingWithConfidenceImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.CompactGPSFixMovingImpl;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sse.common.TimePoint;

public class DynamicGPSFixMovingTrackImpl<ItemType> extends GPSFixTrackImpl<ItemType, GPSFixMoving> implements DynamicGPSFixTrack<ItemType, GPSFixMoving>{
    private static final long serialVersionUID = 9111448573301259784L;
    private static final double MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING = 2;

    public DynamicGPSFixMovingTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        super(trackedItem, millisecondsOverWhichToAverage);
    }
    
    /**
     * @param maxSpeedForSmoothening pass <code>null</code> if you don't want speed-based smoothening
     */
    public DynamicGPSFixMovingTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage, Speed maxSpeedForSmoothening) {
        super(trackedItem, millisecondsOverWhichToAverage, maxSpeedForSmoothening);
    }

    /**
     * This redefinition packs the <code>gpsFix</code> into a more compact representation that conserves
     * memory compared to the original, "naive" implementation. It gets along with a single object.
     */
    @Override
    public void addGPSFix(GPSFixMoving gpsFix) {
        add(gpsFix);
    }
    
    @Override
    public boolean add(GPSFixMoving fix) {
        return super.add(new CompactGPSFixMovingImpl(fix));
    }

    @Override
    protected SpeedWithBearingWithConfidence<TimePoint> getEstimatedSpeed(TimePoint at,
            NavigableSet<GPSFixMoving> fixesToUseForSpeedEstimation, Weigher<TimePoint> weigher) {
        lockForRead();
        try {
            List<GPSFixMoving> relevantFixes = getFixesRelevantForSpeedEstimation(at, fixesToUseForSpeedEstimation);
            List<SpeedWithConfidence<TimePoint>> speeds = new ArrayList<SpeedWithConfidence<TimePoint>>();
            BearingWithConfidenceCluster<TimePoint> bearingCluster = new BearingWithConfidenceCluster<TimePoint>(weigher);
            if (!relevantFixes.isEmpty()) {
                int i=0;
                GPSFixMoving last = relevantFixes.get(i);
                // if speed is within reasonable bounds, add fix's own speed/bearing; this also works if only one
                // "relevant" fix is found; exclude SOG/COG of fixes with SOG/COG==0/0
                if ((last.getSpeed().getBearing().getDegrees() != 0 || last.getSpeed().getKnots() > 0) && (maxSpeedForSmoothing == null || last.getSpeed().compareTo(maxSpeedForSmoothing) <= 0)) {
                    SpeedWithConfidenceImpl<TimePoint> speedWithConfidence = new SpeedWithConfidenceImpl<TimePoint>(
                            last.getSpeed(),
                            /* original confidence */0.9, last.getTimePoint());
                    speeds.add(speedWithConfidence);
                    bearingCluster.add(new BearingWithConfidenceImpl<TimePoint>(last.getSpeed().getBearing(), /* confidence */
                    0.9, last.getTimePoint()));
                }
                while (i<relevantFixes.size()-1) {
                    // add to average the position and time difference
                    GPSFixMoving next = relevantFixes.get(++i);
                    aggregateSpeedAndBearingFromLastToNext(speeds, bearingCluster, last, next);
                    // add to average the speed and bearing provided by the GPSFixMoving
                    // if speed is within reasonable bounds, add fix's own speed/bearing; this also works if only one
                    // "relevant" fix is found; exclude announced SOG/COG if 0/0
                    if ((last.getSpeed().getBearing().getDegrees() != 0 || last.getSpeed().getKnots() > 0)
                            && (maxSpeedForSmoothing == null || next.getSpeed().compareTo(maxSpeedForSmoothing) <= 0)) {
                        SpeedWithConfidenceImpl<TimePoint> computedSpeedWithConfidence = new SpeedWithConfidenceImpl<TimePoint>(
                                next.getSpeed(), /* original confidence */0.9, next.getTimePoint());
                        speeds.add(computedSpeedWithConfidence);
                        bearingCluster.add(new BearingWithConfidenceImpl<TimePoint>(next.getSpeed().getBearing(), /* confidence */
                        0.9, next.getTimePoint()));
                    }
                    last = next;
                }
            }
            ConfidenceBasedAverager<Double, Speed, TimePoint> speedAverager = ConfidenceFactory.INSTANCE
                    .createAverager(weigher);
            HasConfidence<Double, Speed, TimePoint> speedWithConfidence = speedAverager.getAverage(speeds, at);
            BearingWithConfidence<TimePoint> bearingAverage = bearingCluster.getAverage(at);
            Bearing bearing = bearingAverage == null ? null : bearingAverage.getObject();
            SpeedWithBearing avgSpeed = (speedWithConfidence == null || bearing == null) ? null
                    : new KnotSpeedWithBearingImpl(speedWithConfidence.getObject().getKnots(), bearing);
            SpeedWithBearingWithConfidence<TimePoint> result = speedWithConfidence == null || bearingAverage == null ? null
                    : new SpeedWithBearingWithConfidenceImpl<TimePoint>(
                            avgSpeed,
                            /* confidence */((speedWithConfidence == null ? 0.0 : speedWithConfidence.getConfidence()) + (bearingAverage == null ? 0.0
                                    : bearingAverage.getConfidence())) / 2., at);
            return result;
        } finally {
            unlockAfterRead();
        }
    }
    
    /**
     * In addition to the base class implementation, we may have the speed and bearing as measured by the device (the
     * special speed/bearing combination 0.0/0.0 is simply ignored, as are fix-provided speed values that exceed
     * {@link #maxSpeedForSmoothing}). If the adjacent fixes are within the averaging interval defined by
     * {@link GPSFixTrackImpl#getMillisecondsOverWhichToAverageSpeed()}, we use the device-measured speed and compare it
     * with the speed computed based on the time stamp and distance between previous and next fix. If the ratio between
     * the higher and the lower of the two speeds exceeds
     * {@link #MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING}, the fix is considered invalid.
     */
    @Override
    protected boolean isValid(NavigableSet<GPSFixMoving> rawFixes, GPSFixMoving e) {
        assertReadLock();
        final boolean isValid;
        if (e.isValidityCached()) {
            isValid = e.isValidCached();
        } else {
            boolean fixHasValidSogAndCog = (e.getSpeed().getMetersPerSecond() != 0.0 || e.getSpeed().getBearing().getDegrees() != 0.0) &&
                    (maxSpeedForSmoothing == null || e.getSpeed().compareTo(maxSpeedForSmoothing) <= 0);

            GPSFixMoving previous = rawFixes.lower(e);
            final boolean atLeastOnePreviousFixInRange = previous != null && e.getTimePoint().asMillis() - previous.getTimePoint().asMillis() <= getMillisecondsOverWhichToAverageSpeed();
            Speed speedToPrevious = null;
            boolean foundValidPreviousFixInRange = false;
            while (previous != null && !foundValidPreviousFixInRange && e.getTimePoint().asMillis() - previous.getTimePoint().asMillis() <= getMillisecondsOverWhichToAverageSpeed()) {
                speedToPrevious = previous.getPosition().getDistance(e.getPosition())
                        .inTime(e.getTimePoint().asMillis() - previous.getTimePoint().asMillis());
                final double speedToPreviousFactor;
                if (speedToPrevious.getMetersPerSecond() >= e.getSpeed().getMetersPerSecond()) {
                    speedToPreviousFactor = speedToPrevious.getMetersPerSecond() / e.getSpeed().getMetersPerSecond();
                } else {
                    speedToPreviousFactor = e.getSpeed().getMetersPerSecond() / speedToPrevious.getMetersPerSecond();
                }
                foundValidPreviousFixInRange = (maxSpeedForSmoothing == null || speedToPrevious.compareTo(maxSpeedForSmoothing) <= 0)
                        && (!fixHasValidSogAndCog || speedToPreviousFactor <= MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING);
                previous = rawFixes.lower(previous);
            }
            boolean foundValidNextFixInRange = false;
            boolean atLeastOneNextFixInRange = false;
            // only spend the effort to calculate the "next"-related predicate if the "previous"-related part of the disjunction below isn't already false
            if (!atLeastOnePreviousFixInRange || foundValidPreviousFixInRange) {
                GPSFixMoving next = rawFixes.higher(e);
                atLeastOneNextFixInRange = next != null && next.getTimePoint().asMillis() - e.getTimePoint().asMillis() <= getMillisecondsOverWhichToAverageSpeed();
                Speed speedToNext = null;
                while (next != null && !foundValidNextFixInRange && next.getTimePoint().asMillis() - e.getTimePoint().asMillis() <= getMillisecondsOverWhichToAverageSpeed()) {
                    speedToNext = e.getPosition().getDistance(next.getPosition())
                            .inTime(next.getTimePoint().asMillis() - e.getTimePoint().asMillis());
                    final double speedToNextFactor;
                    if (speedToNext.getMetersPerSecond() >= e.getSpeed().getMetersPerSecond()) {
                        speedToNextFactor = speedToNext.getMetersPerSecond() / e.getSpeed().getMetersPerSecond();
                    } else {
                        speedToNextFactor = e.getSpeed().getMetersPerSecond() / speedToNext.getMetersPerSecond();
                    }
                    foundValidNextFixInRange = (maxSpeedForSmoothing == null || speedToNext.compareTo(maxSpeedForSmoothing) <= 0)
                            && (!fixHasValidSogAndCog || speedToNextFactor <= MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING);
                    next = rawFixes.higher(next);
                }
            }
            isValid = (!atLeastOnePreviousFixInRange || foundValidPreviousFixInRange) && (!atLeastOneNextFixInRange || foundValidNextFixInRange);
            e.cacheValidity(isValid);
        }
        return isValid;
    }

    @Override
    public void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage) {
        super.setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverage);
    }
    
}
