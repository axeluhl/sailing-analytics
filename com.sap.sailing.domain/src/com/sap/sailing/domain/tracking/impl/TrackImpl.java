package com.sap.sailing.domain.tracking.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Track;

public class TrackImpl<ItemType, FixType extends GPSFix> implements Track<ItemType, FixType> {
    private final ItemType trackedItem;

    /**
     * The fixes, ordered by their time points
     */
    private final NavigableSet<GPSFix> fixes;
    
    private class DummyGPSFixWithDateOnly implements GPSFix {
        private final TimePoint timePoint;
        public DummyGPSFixWithDateOnly(TimePoint timePoint) {
            super();
            this.timePoint = timePoint;
        }

        @Override
        public Position getPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TimePoint getTimePoint() {
            return timePoint;
        }
        
    }
    
    public TrackImpl(ItemType trackedItem) {
        super();
        this.trackedItem = trackedItem;
        this.fixes = new TreeSet<GPSFix>(TimedComparator.INSTANCE);
    }
    
    protected NavigableSet<GPSFix> getInternalFixes() {
        return fixes;
    }

    @Override
    public ItemType getTrackedItem() {
        return trackedItem;
    }
    
    
    /**
     * Iterates the fixes in the order of their time points
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterable<FixType> getFixes() {
        return (Iterable<FixType>) Collections.unmodifiableSet(fixes);
    }

    @Override
    public Position getEstimatedPosition(TimePoint timePoint) {
        FixType lastFixAtOrBefore = getLastFixAtOrBefore(timePoint);
        FixType firstFixAtOrAfter = getFirstFixAtOrAfter(timePoint);
        if (lastFixAtOrBefore != null && lastFixAtOrBefore == firstFixAtOrAfter) {
            return lastFixAtOrBefore.getPosition(); // exact match; how unlikely is that?
        } else {
            SpeedWithBearing estimatedSpeed = estimateSpeed(lastFixAtOrBefore, firstFixAtOrAfter);
            if (estimatedSpeed == null) {
                return null;
            } else {
                if (lastFixAtOrBefore != null) {
                    Distance distance = estimatedSpeed.travel(lastFixAtOrBefore.getTimePoint(), timePoint);
                    Position result = lastFixAtOrBefore.getPosition()
                            .translateGreatCircle(estimatedSpeed.getBearing(),
                                    distance);
                    return result;
                } else {
                    // firstFixAtOrAfter can't be null because otherwise no speed could have been estimated
                    return firstFixAtOrAfter.getPosition();
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
            if (fix1 instanceof GPSFixMoving) {
                return ((GPSFixMoving) fix1).getSpeed();
            } else {
                return null;
            }
        } else {
            Distance distance = fix1.getPosition().getDistance(fix2.getPosition());
            long millis = Math.abs(fix1.getTimePoint().asMillis() - fix2.getTimePoint().asMillis());
            SpeedWithBearing speed = new KnotSpeedImpl(distance.getNauticalMiles() / (millis / 1000.),
                    fix1.getPosition().getBearingGreatCircle(fix2.getPosition()));
            return speed;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastFixAtOrBefore(TimePoint timePoint) {
        return (FixType) fixes.floor(new DummyGPSFixWithDateOnly(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstFixAtOrAfter(TimePoint timePoint) {
        return (FixType) fixes.ceiling(new DummyGPSFixWithDateOnly(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getLastFixBefore(TimePoint timePoint) {
        return (FixType) fixes.lower(new DummyGPSFixWithDateOnly(timePoint));
    }

    @SuppressWarnings("unchecked")
    @Override
    public FixType getFirstFixAfter(TimePoint timePoint) {
        return (FixType) fixes.higher(new DummyGPSFixWithDateOnly(timePoint));
    }

    @Override
    public Distance getDistanceTraveled(TimePoint from, TimePoint to) {
        double distanceInNauticalMiles = 0;
        if (from.compareTo(to) < 0) {
            Position fromPos = getEstimatedPosition(from);
            NavigableSet<GPSFix> subset = fixes.subSet(new DummyGPSFixWithDateOnly(from),
            /* fromInclusive */false, new DummyGPSFixWithDateOnly(to),
            /* toInclusive */false);
            for (GPSFix fix : subset) {
                distanceInNauticalMiles += fromPos.getDistance(fix.getPosition()).getNauticalMiles();
                fromPos = fix.getPosition();
            }
            Position toPos = getEstimatedPosition(to);
            distanceInNauticalMiles += fromPos.getDistance(toPos).getNauticalMiles();
        }
        return new NauticalMileDistance(distanceInNauticalMiles);
    }

    @Override
    public Iterator<FixType> getFixes(TimePoint startingAt, boolean inclusive) {
        @SuppressWarnings("unchecked")
        Iterator<FixType> result = (Iterator<FixType>) getInternalFixes().tailSet(
                new DummyGPSFixWithDateOnly(startingAt), inclusive).iterator();
        return result;
    }

}
