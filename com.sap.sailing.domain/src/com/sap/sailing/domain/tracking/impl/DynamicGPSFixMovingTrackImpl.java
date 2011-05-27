package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.tracking.GPSFixMoving;

public class DynamicGPSFixMovingTrackImpl<ItemType> extends DynamicTrackImpl<ItemType, GPSFixMoving> {

    public DynamicGPSFixMovingTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        super(trackedItem, millisecondsOverWhichToAverage);
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
    public SpeedWithBearing getEstimatedSpeed(TimePoint at) {
        DummyGPSFixMoving atTimed = new DummyGPSFixMoving(at);
        NavigableSet<GPSFixMoving> beforeSet = getInternalFixes().headSet(atTimed, /* inclusive */ true);
        NavigableSet<GPSFixMoving> afterSet = getInternalFixes().tailSet(atTimed, /* inclusive */ true);
        List<GPSFixMoving> relevantFixes = new LinkedList<GPSFixMoving>();
        for (GPSFixMoving beforeFix : beforeSet.descendingSet()) {
            if (at.asMillis() - beforeFix.getTimePoint().asMillis() > getMillisecondsOverWhichToAverage()/2) {
                break;
            }
            relevantFixes.add(0, beforeFix);
        }
        for (GPSFixMoving afterFix : afterSet) {
            if (at.asMillis() - afterFix.getTimePoint().asMillis() > getMillisecondsOverWhichToAverage()/2) {
                break;
            }
            relevantFixes.add(afterFix);
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
        SpeedWithBearing avgSpeed = new KnotSpeedImpl(knotSum / count, new DegreeBearingImpl(bearingDegSum/count));
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
    
}
