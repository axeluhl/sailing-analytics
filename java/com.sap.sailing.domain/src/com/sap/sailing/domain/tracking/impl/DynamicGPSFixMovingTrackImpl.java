package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class DynamicGPSFixMovingTrackImpl<ItemType> extends DynamicTrackImpl<ItemType, GPSFixMoving> {
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
    protected SpeedWithBearing getEstimatedSpeed(TimePoint at, NavigableSet<GPSFixMoving> fixesToUseForSpeedEstimation) {
        List<GPSFixMoving> relevantFixes = getFixesRelevantForSpeedEstimation(at, fixesToUseForSpeedEstimation);
        double knotSum = 0;
        BearingCluster bearingCluster = new BearingCluster();
        int count = 0;
        if (!relevantFixes.isEmpty()) {
            Iterator<GPSFixMoving> fixIter = relevantFixes.iterator();
            GPSFixMoving last = fixIter.next();
            knotSum = last.getSpeed().getKnots();
            bearingCluster.add(last.getSpeed().getBearing());
            count = 1;
            while (fixIter.hasNext()) {
                // add to average the position and time difference
                GPSFixMoving next = fixIter.next();
                knotSum += last.getPosition().getDistance(next.getPosition())
                        .inTime(next.getTimePoint().asMillis() - last.getTimePoint().asMillis()).getKnots();
                bearingCluster.add(last.getPosition().getBearingGreatCircle(next.getPosition()));
                count++;
                
                // add to average the speed and bearing provided by the GPSFixMoving
                knotSum += next.getSpeed().getKnots();
                bearingCluster.add(next.getSpeed().getBearing());
                count++;
                last = next;
            }
        }
        SpeedWithBearing avgSpeed = count == 0 ? null : new KnotSpeedWithBearingImpl(knotSum / count, bearingCluster.getAverage());
        return avgSpeed;
    }
    
    private List<GPSFixMoving> getFixesRelevantForSpeedEstimation(TimePoint at,
            NavigableSet<GPSFixMoving> fixesToUseForSpeedEstimation) {
        // TODO factor out the obtaining of relevant fixes which should be the same in super.getEstimatedSpeed(at)
        DummyGPSFixMoving atTimed = new DummyGPSFixMoving(at);
        NavigableSet<GPSFixMoving> beforeSet = fixesToUseForSpeedEstimation.headSet(atTimed, /* inclusive */ false);
        NavigableSet<GPSFixMoving> afterSet = fixesToUseForSpeedEstimation.tailSet(atTimed, /* inclusive */ true);
        List<GPSFixMoving> relevantFixes = new LinkedList<GPSFixMoving>();
        for (GPSFixMoving beforeFix : beforeSet.descendingSet()) {
            if (at.asMillis() - beforeFix.getTimePoint().asMillis() > getMillisecondsOverWhichToAverage()/2) {
                break;
            }
            relevantFixes.add(0, beforeFix);
        }
        for (GPSFixMoving afterFix : afterSet) {
            if (afterFix.getTimePoint().asMillis() - at.asMillis() > getMillisecondsOverWhichToAverage()/2) {
                break;
            }
            relevantFixes.add(afterFix);
        }
        if (relevantFixes.isEmpty()) {
            // find the fix closest to "at":
            if (beforeSet.isEmpty()) {
                if (!afterSet.isEmpty()) {
                    relevantFixes.add(afterSet.first());
                }
            } else {
                if (afterSet.isEmpty()) {
                    relevantFixes.add(beforeSet.last());
                } else {
                    GPSFixMoving beforeFix = beforeSet.last();
                    GPSFixMoving afterFix = afterSet.first();
                    relevantFixes.add(at.asMillis() - beforeFix.getTimePoint().asMillis() <= afterFix.getTimePoint()
                            .asMillis() - at.asMillis() ? beforeFix : afterFix);
                }
            }
        }
        return relevantFixes;
    }

    private class DummyGPSFixMoving extends DummyTimed implements GPSFixMoving {
        public DummyGPSFixMoving(TimePoint timePoint) {
            super(timePoint);
        }
        @Override
        public Position getPosition() {
            return null;
        }
        @Override
        public SpeedWithBearing getSpeed() {
            return null;
        }
        @Override
        public SpeedWithBearing getSpeedAndBearingRequiredToReach(GPSFix to) {
            return null;
        }
        @Override
        public boolean isValidityCached() {
            return false;
        }
        @Override
        public boolean isValid() {
            return false;
        }
        @Override
        public void invalidateCache() {
            
        }
        @Override
        public void cacheValidity(boolean isValid) {
        }
    }
    
    
    @Override
    protected Speed getSpeed(GPSFixMoving fix, Position lastPos, TimePoint timePointOfLastPos) {
        Speed fixSpeed = fix.getSpeed();
        Speed calculatedSpeed = super.getSpeed(fix, lastPos, timePointOfLastPos);
        Speed averaged = averageSpeed(fixSpeed, calculatedSpeed);
        return averaged;
    }

    private Speed averageSpeed(Speed... speeds) {
        double sumInKMH = 0;
        int count = 0;
        for (Speed speed : speeds) {
            sumInKMH += speed.getKilometersPerHour();
            count++;
        }
        return new KilometersPerHourSpeedImpl(sumInKMH/count);
    }

    /**
     * In addition to the base class implementation, we additionally have the speed and bearing as
     * measured by the device. We use the device-measured speed and compare it with the speed computed
     * based on the timestamp and distance between previous and next fix. If the latter speed exceeds the
     * measured speed by more than a factor of {@link #MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING},
     * the fix is considered invalid.
     */
    @Override
    protected boolean isValid(PartialNavigableSetView<GPSFixMoving> filteredView, GPSFixMoving e) {
        boolean result;
        if (e.isValidityCached()) {
            result = e.isValid();
        } else {
            GPSFixMoving previous = filteredView.lowerInternal(e);
            GPSFixMoving next = filteredView.higherInternal(e);
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
                    && (maxSpeedForSmoothening == null
                            || (previous == null || speedToPrevious.compareTo(maxSpeedForSmoothening) <= 0) || (next == null || speedToNext
                            .compareTo(maxSpeedForSmoothening) <= 0));
            e.cacheValidity(result);
        }
        return result;
    }
}
