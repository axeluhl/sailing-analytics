package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.BearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.BearingWithConfidenceImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.ConfidenceFactory;
import com.sap.sailing.domain.confidence.HasConfidence;
import com.sap.sailing.domain.confidence.Weigher;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class DynamicGPSFixMovingTrackImpl<ItemType> extends DynamicTrackImpl<ItemType, GPSFixMoving> {
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
        super.addGPSFix(new CompactGPSFixMovingImpl(gpsFix));
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
                // add fix's own speed/bearing; this also works if only one "relevant" fix is found
                SpeedWithConfidenceImpl<TimePoint> speedWithConfidence = new SpeedWithConfidenceImpl<TimePoint>(
                        last.getSpeed(),
                        /* original confidence */0.9, last.getTimePoint());
                speeds.add(speedWithConfidence);
                bearingCluster.add(new BearingWithConfidenceImpl<TimePoint>(last.getSpeed().getBearing(), /* confidence */
                        0.9, last.getTimePoint()));
                while (i<relevantFixes.size()-1) {
                    // add to average the position and time difference
                    GPSFixMoving next = relevantFixes.get(++i);
                    aggregateSpeedAndBearingFromLastToNext(speeds, bearingCluster, last, next);
                    // add to average the speed and bearing provided by the GPSFixMoving
                    SpeedWithConfidenceImpl<TimePoint> computedSpeedWithConfidence = new SpeedWithConfidenceImpl<TimePoint>(
                            next.getSpeed(), /* original confidence */0.9, next.getTimePoint());
                    speeds.add(computedSpeedWithConfidence);
                    bearingCluster.add(new BearingWithConfidenceImpl<TimePoint>(next.getSpeed().getBearing(), /* confidence */
                            0.9, next.getTimePoint()));
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
     * In addition to the base class implementation, we additionally have the speed and bearing as
     * measured by the device. We use the device-measured speed and compare it with the speed computed
     * based on the timestamp and distance between previous and next fix. If the latter speed exceeds the
     * measured speed by more than a factor of {@link #MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING},
     * the fix is considered invalid.
     */
    @Override
    protected boolean isValid(NavigableSet<GPSFixMoving> rawFixes, GPSFixMoving e) {
        assertReadLock();
        boolean result;
        if (e.isValidityCached()) {
            result = e.isValid();
        } else {
            GPSFixMoving previous = rawFixes.lower(e);
            GPSFixMoving next = rawFixes.higher(e);
            Speed speedToPrevious = Speed.NULL;
            if (previous != null) {
                speedToPrevious = previous.getPosition().getDistance(e.getPosition())
                        .inTime(e.getTimePoint().asMillis() - previous.getTimePoint().asMillis());
            }
            Speed speedToNext = Speed.NULL;
            if (next != null) {
                speedToNext = e.getPosition().getDistance(next.getPosition())
                        .inTime(next.getTimePoint().asMillis() - e.getTimePoint().asMillis());
            }
            result = (previous == null || speedToPrevious.getMetersPerSecond() <= MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING
                    * e.getSpeed().getMetersPerSecond())
                    && (next == null || speedToNext.getMetersPerSecond() <= MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING
                            * e.getSpeed().getMetersPerSecond())
                    && (maxSpeedForSmoothing == null
                            || (previous == null || speedToPrevious.compareTo(maxSpeedForSmoothing) <= 0) || (next == null || speedToNext
                            .compareTo(maxSpeedForSmoothing) <= 0));
            e.cacheValidity(result);
        }
        return result;
    }
}
