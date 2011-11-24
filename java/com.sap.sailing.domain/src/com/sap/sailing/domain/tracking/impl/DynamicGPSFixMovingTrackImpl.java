package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class DynamicGPSFixMovingTrackImpl<ItemType> extends DynamicTrackImpl<ItemType, GPSFixMoving> {
    // private static final double MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING = 2;

    public DynamicGPSFixMovingTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        super(trackedItem, millisecondsOverWhichToAverage);
    }

    /**
     * This redefinition packs the <code>gpsFix</code> into a more compact representation that conserves
     * memory compared to the original, "naive" implementation. It gets along with a single object.
     */
    @Override
    public synchronized void addGPSFix(GPSFixMoving gpsFix) {
        super.addGPSFix(new CompactGPSFixMovingImpl(gpsFix));
    }

    /**
     * Here we know for sure that the GPS fixes are {@link GPSFixMoving} instances,
     * so we can use their {@link GPSFixMoving#getSpeed() speed} in averaging. We're still
     * using an interval of {@link #getMillisecondsOverWhichToAverage()} around <code>at</code>,
     * but this time we add the speeds and bearings provided by the fix onto the values for
     * averaging, so the result considers both, the GPS-provided speeds and bearings as well as
     * the speeds/bearings determined by distance/time difference of the fixes themselves.
     */
    @Override
    public synchronized SpeedWithBearing getEstimatedSpeed(TimePoint at) {
        // TODO factor out the obtaining of relevant fixes which should be the same in super.getEstimatedSpeed(at)
        DummyGPSFixMoving atTimed = new DummyGPSFixMoving(at);
        NavigableSet<GPSFixMoving> beforeSet = getInternalFixes().headSet(atTimed, /* inclusive */ true);
        NavigableSet<GPSFixMoving> afterSet = getInternalFixes().tailSet(atTimed, /* inclusive */ false);
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
        double knotSum = 0;
        double bearingDegSum = 0;
        int count = 0;
        if (!relevantFixes.isEmpty()) {
            Iterator<GPSFixMoving> fixIter = relevantFixes.iterator();
            GPSFixMoving last = fixIter.next();
            knotSum = last.getSpeed().getKnots();
            bearingDegSum = last.getSpeed().getBearing().getDegrees();
            count = 1;
            while (fixIter.hasNext()) {
                // add to average the position and time difference
                GPSFixMoving next = fixIter.next();
                knotSum += last.getPosition().getDistance(next.getPosition())
                        .inTime(next.getTimePoint().asMillis() - last.getTimePoint().asMillis()).getKnots();
                bearingDegSum += last.getPosition().getBearingGreatCircle(next.getPosition()).getDegrees();
                count++;
                
                // add to average the speed and bearing provided by the GPSFixMoving
                knotSum += next.getSpeed().getKnots();
                bearingDegSum += next.getSpeed().getBearing().getDegrees();
                count++;
                last = next;
            }
        }
        SpeedWithBearing avgSpeed = new KnotSpeedWithBearingImpl(knotSum / count, new DegreeBearingImpl(bearingDegSum/count));
        return avgSpeed;
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
    /* TODO first debug this and adjust any tests accordingly
    @Override
    protected boolean isValid(PartialNavigableSetView<GPSFixMoving> filteredView, GPSFixMoving e) {
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
        return speedToPrevious.getMetersPerSecond() <= MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING*e.getSpeed().getMetersPerSecond() &&
                speedToNext.getMetersPerSecond() <= MAX_SPEED_FACTOR_COMPARED_TO_MEASURED_SPEED_FOR_FILTERING*e.getSpeed().getMetersPerSecond() &&
                (speedToPrevious.compareTo(MAX_SPEED_FOR_SMOOTHNING) <= 0 || speedToNext.compareTo(MAX_SPEED_FOR_SMOOTHNING) <= 0); 
    }
    */
}
