package com.sap.sailing.domain.tracking.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;

public class GPSFixTrackImpl<ItemType, FixType extends GPSFix> extends TrackImpl<FixType> implements GPSFixTrack<ItemType, FixType> {
    private final ItemType trackedItem;

    public GPSFixTrackImpl(ItemType trackedItem) {
        super();
        this.trackedItem = trackedItem;
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
    
    @Override
    public ItemType getTrackedItem() {
        return trackedItem;
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

    
    private NavigableSet<GPSFix> getGPSFixes() {
        @SuppressWarnings("unchecked")
        NavigableSet<GPSFix> result = (NavigableSet<GPSFix>) super.getInternalFixes();
        return result;
    }

    @Override
    public Distance getDistanceTraveled(TimePoint from, TimePoint to) {
        double distanceInNauticalMiles = 0;
        if (from.compareTo(to) < 0) {
            Position fromPos = getEstimatedPosition(from);
            NavigableSet<GPSFix> subset = getGPSFixes().subSet(new DummyGPSFix(from),
            /* fromInclusive */false, new DummyGPSFix(to),
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

}
