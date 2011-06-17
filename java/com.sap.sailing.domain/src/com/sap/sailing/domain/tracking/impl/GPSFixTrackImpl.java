package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;

public class GPSFixTrackImpl<ItemType, FixType extends GPSFix> extends TrackImpl<FixType> implements GPSFixTrack<ItemType, FixType> {
    private final ItemType trackedItem;
    private long millisecondsOverWhichToAverage;

    public GPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        super();
        this.trackedItem = trackedItem;
        this.millisecondsOverWhichToAverage = millisecondsOverWhichToAverage;
    }
    
    private class DummyGPSFix extends DummyTimed implements GPSFix {
        public DummyGPSFix(TimePoint timePoint) {
            super(timePoint);
        }
        @Override
        public Position getPosition() {
            return null;
        }
    }
    
    protected void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage) {
        this.millisecondsOverWhichToAverage = millisecondsOverWhichToAverage;
    }
    
    @Override
    public ItemType getTrackedItem() {
        return trackedItem;
    }
    
    @Override
    public Position getEstimatedPosition(TimePoint timePoint, boolean extrapolate) {
        FixType lastFixAtOrBefore = getLastFixAtOrBefore(timePoint);
        FixType firstFixAtOrAfter = getFirstFixAtOrAfter(timePoint);
        if (lastFixAtOrBefore != null && lastFixAtOrBefore == firstFixAtOrAfter) {
            return lastFixAtOrBefore.getPosition(); // exact match; how unlikely is that?
        } else {
            if (firstFixAtOrAfter == null && !extrapolate) {
                return lastFixAtOrBefore == null ? null : lastFixAtOrBefore.getPosition();
            } else {
                SpeedWithBearing estimatedSpeed = estimateSpeed(lastFixAtOrBefore, firstFixAtOrAfter);
                if (estimatedSpeed == null) {
                    return null;
                } else {
                    if (lastFixAtOrBefore != null) {
                        Distance distance = estimatedSpeed.travel(lastFixAtOrBefore.getTimePoint(), timePoint);
                        Position result = lastFixAtOrBefore.getPosition().translateGreatCircle(
                                estimatedSpeed.getBearing(), distance);
                        return result;
                    } else {
                        // firstFixAtOrAfter can't be null because otherwise no speed could have been estimated
                        return firstFixAtOrAfter.getPosition();
                    }
                }
            }
        }
    }

    private SpeedWithBearing estimateSpeed(FixType fix1, FixType fix2) {
        if (fix1 == null) {
            if (fix2 instanceof GPSFixMoving) {
                return ((GPSFixMoving) fix2).getSpeed();
            } else {
                return null;
            }
        } else if (fix2 == null) {
            FixType lastBeforeFix1 = getLastFixBefore(fix1.getTimePoint());
            if (lastBeforeFix1 != null) {
                fix2 = fix1;
                fix1 = lastBeforeFix1; // compute speed based on the last two fixes and assume constant speed
            } else {
                if (fix1 instanceof GPSFixMoving) {
                    return ((GPSFixMoving) fix1).getSpeed();
                } else {
                    return null;
                }
            }
        }
        Distance distance = fix1.getPosition().getDistance(fix2.getPosition());
        long millis = Math.abs(fix1.getTimePoint().asMillis() - fix2.getTimePoint().asMillis());
        SpeedWithBearing speed = new KnotSpeedWithBearingImpl(distance.getNauticalMiles() / (millis / 1000. / 3600.),
                fix1.getPosition().getBearingGreatCircle(fix2.getPosition()));
        return speed;
    }

    
    private NavigableSet<GPSFix> getGPSFixes() {
        @SuppressWarnings("unchecked")
        NavigableSet<GPSFix> result = (NavigableSet<GPSFix>) super.getInternalFixes();
        return result;
    }

    @Override
    public Distance getDistanceTraveled(TimePoint from, TimePoint to) {
        double distanceInNauticalMiles = 0;
        if (from.compareTo(to) < 0) {
            Position fromPos = getEstimatedPosition(from, false);
            if (fromPos == null) {
                return Distance.NULL;
            }
            NavigableSet<GPSFix> subset = getGPSFixes().subSet(new DummyGPSFix(from),
            /* fromInclusive */false, new DummyGPSFix(to),
            /* toInclusive */false);
            for (GPSFix fix : subset) {
                distanceInNauticalMiles += fromPos.getDistance(fix.getPosition()).getNauticalMiles();
                fromPos = fix.getPosition();
            }
            Position toPos = getEstimatedPosition(to, false);
            distanceInNauticalMiles += fromPos.getDistance(toPos).getNauticalMiles();
            return new NauticalMileDistance(distanceInNauticalMiles);
        } else {
            return Distance.NULL;
        }
    }

    /**
     * Since we don't know for sure whether the GPS fixes are {@link GPSFixMoving} instances, here we only estimate
     * speed based on the distance and time between the fixes, averaged over an interval of
     * {@link #millisecondsOverWhichToAverage} milliseconds around <code>at</code>. Subclasses that know about the
     * particular fix type may redefine this to exploit a {@link SpeedWithBearing} attached, e.g., to a
     * {@link GPSFixMoving}.
     */
    @Override
    public SpeedWithBearing getEstimatedSpeed(TimePoint at) {
        DummyGPSFix atTimed = new DummyGPSFix(at);
        NavigableSet<GPSFix> beforeSet = getGPSFixes().headSet(atTimed, /* inclusive */ true);
        NavigableSet<GPSFix> afterSet = getGPSFixes().tailSet(atTimed, /* inclusive */ true);
        List<GPSFix> relevantFixes = new LinkedList<GPSFix>();
        for (GPSFix beforeFix : beforeSet.descendingSet()) {
            if (at.asMillis() - beforeFix.getTimePoint().asMillis() > getMillisecondsOverWhichToAverage()/2) {
                break;
            }
            relevantFixes.add(0, beforeFix);
        }
        for (GPSFix afterFix : afterSet) {
            if (at.asMillis() - afterFix.getTimePoint().asMillis() > getMillisecondsOverWhichToAverage()/2) {
                break;
            }
            relevantFixes.add(afterFix);
        }
        double knotSum = 0;
        double bearingDegSum = 0;
        int count = 0;
        if (!relevantFixes.isEmpty()) {
            Iterator<GPSFix> fixIter = relevantFixes.iterator();
            GPSFix last = fixIter.next();
            while (fixIter.hasNext()) {
                GPSFix next = fixIter.next();
                knotSum += last.getPosition().getDistance(next.getPosition())
                        .inTime(next.getTimePoint().asMillis() - last.getTimePoint().asMillis()).getKnots();
                bearingDegSum += last.getPosition().getBearingGreatCircle(next.getPosition()).getDegrees();
                count++;
                last = next;
            }
        }
        SpeedWithBearing avgSpeed = new KnotSpeedWithBearingImpl(knotSum / count, new DegreeBearingImpl(bearingDegSum/count));
        return avgSpeed;
    }

    protected long getMillisecondsOverWhichToAverage() {
        return millisecondsOverWhichToAverage;
    }

}
