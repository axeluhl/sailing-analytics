package com.sap.sailing.domain.tracking.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.NauticalMileDistance;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.WithValidityCache;

public class GPSFixTrackImpl<ItemType, FixType extends GPSFix> extends TrackImpl<FixType> implements GPSFixTrack<ItemType, FixType> {
    private static final Speed DEFAULT_MAX_SPEED_FOR_SMOOTHING = new KnotSpeedImpl(50);
    protected final Speed maxSpeedForSmoothening;
    
    private final ItemType trackedItem;
    private long millisecondsOverWhichToAverage;

    public GPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        this(trackedItem, millisecondsOverWhichToAverage, DEFAULT_MAX_SPEED_FOR_SMOOTHING);
    }
    
    public GPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage, Speed maxSpeedForSmoothening) {
        super();
        this.trackedItem = trackedItem;
        this.millisecondsOverWhichToAverage = millisecondsOverWhichToAverage;
        this.maxSpeedForSmoothening = maxSpeedForSmoothening;
    }

    private class DummyGPSFix extends DummyTimed implements GPSFix {
        public DummyGPSFix(TimePoint timePoint) {
            super(timePoint);
        }
        @Override
        public Position getPosition() {
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
    
    protected void setMillisecondsOverWhichToAverage(long millisecondsOverWhichToAverage) {
        this.millisecondsOverWhichToAverage = millisecondsOverWhichToAverage;
    }
    
    @Override
    public ItemType getTrackedItem() {
        return trackedItem;
    }
    
    @Override
    public long getMillisecondsOverWhichToAverageSpeed() {
        return millisecondsOverWhichToAverage;
    }

    @Override
    public Position getEstimatedPosition(TimePoint timePoint, boolean extrapolate) {
        FixType lastFixAtOrBefore = getLastFixAtOrBefore(timePoint);
        FixType firstFixAtOrAfter = getFirstFixAtOrAfter(timePoint);
        return getEstimatedPosition(timePoint, extrapolate, lastFixAtOrBefore, firstFixAtOrAfter);
    }

    @Override
    public Position getEstimatedRawPosition(TimePoint timePoint, boolean extrapolate) {
        FixType lastFixAtOrBefore = getLastRawFixAtOrBefore(timePoint);
        FixType firstFixAtOrAfter = getFirstRawFixAtOrAfter(timePoint);
        return getEstimatedPosition(timePoint, extrapolate, lastFixAtOrBefore, firstFixAtOrAfter);
    }

    private Position getEstimatedPosition(TimePoint timePoint, boolean extrapolate, FixType lastFixAtOrBefore,
            FixType firstFixAtOrAfter) {
        if (lastFixAtOrBefore != null && lastFixAtOrBefore == firstFixAtOrAfter) {
            return lastFixAtOrBefore.getPosition(); // exact match; how unlikely is that?
        } else {
            if (lastFixAtOrBefore == null && firstFixAtOrAfter != null) {
                return firstFixAtOrAfter.getPosition(); // asking for time point before first fix: return first fix's position
            }
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

    @Override
    public synchronized Speed getMaximumSpeedOverGround(TimePoint from, TimePoint to) {
        // fetch all fixes on this leg so far and determine their maximum speed
        Iterator<FixType> iter = getFixesIterator(from, /* inclusive */ true);
        Speed max = Speed.NULL;
        if (iter.hasNext()) {
            Position lastPos = getEstimatedPosition(from, false);
            while (iter.hasNext()) {
                FixType fix = iter.next();
                Speed fixSpeed = getSpeed(fix, lastPos, from);
                if (fixSpeed.compareTo(max) > 0) {
                    max = fixSpeed;
                }
            }
        }
        return max;
    }

    protected Speed getSpeed(FixType fix, Position lastPos, TimePoint timePointOfLastPos) {
        return lastPos.getDistance(fix.getPosition()).inTime(fix.getTimePoint().asMillis()-timePointOfLastPos.asMillis());
    }

    private SpeedWithBearing estimateSpeed(FixType fix1, FixType fix2) {
        if (fix1 == null) {
            if (fix2 instanceof GPSFixMoving) {
                return ((GPSFixMoving) fix2).getSpeed();
            } else {
                return null;
            }
        } else if (fix2 == null) {
            FixType lastBeforeFix1 = getLastRawFixBefore(fix1.getTimePoint());
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

    
    /**
     * Returns the smoothened fixes (see {@link #getInternalFixes()}), type-cast such that it's a set of {@link GPSFix}
     * objects
     */
    private NavigableSet<GPSFix> getGPSFixes() {
        @SuppressWarnings("unchecked")
        NavigableSet<GPSFix> result = (NavigableSet<GPSFix>) getInternalFixes();
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
            synchronized (this) {
                NavigableSet<GPSFix> subset = getGPSFixes().subSet(new DummyGPSFix(from),
                /* fromInclusive */false, new DummyGPSFix(to),
                /* toInclusive */false);
                for (GPSFix fix : subset) {
                    double distanceBetweenAdjacentFixesInNauticalMiles = fromPos.getDistance(fix.getPosition()).getNauticalMiles();
                    distanceInNauticalMiles += distanceBetweenAdjacentFixesInNauticalMiles;
                    fromPos = fix.getPosition();
                }
            }
            Position toPos = getEstimatedPosition(to, false);
            distanceInNauticalMiles += fromPos.getDistance(toPos).getNauticalMiles();
            return new NauticalMileDistance(distanceInNauticalMiles);
        } else {
            return Distance.NULL;
        }
    }

    @Override
    public Distance getRawDistanceTraveled(TimePoint from, TimePoint to) {
        double distanceInNauticalMiles = 0;
        if (from.compareTo(to) < 0) {
            Position fromPos = getEstimatedRawPosition(from, false);
            if (fromPos == null) {
                return Distance.NULL;
            }
            @SuppressWarnings("unchecked")
            NavigableSet<GPSFix> subset = (NavigableSet<GPSFix>) getInternalRawFixes().subSet((FixType) new DummyGPSFix(from),
            /* fromInclusive */false, (FixType) new DummyGPSFix(to),
            /* toInclusive */false);
            for (GPSFix fix : subset) {
                distanceInNauticalMiles += fromPos.getDistance(fix.getPosition()).getNauticalMiles();
                fromPos = fix.getPosition();
            }
            Position toPos = getEstimatedRawPosition(to, false);
            distanceInNauticalMiles += fromPos.getDistance(toPos).getNauticalMiles();
            return new NauticalMileDistance(distanceInNauticalMiles);
        } else {
            return Distance.NULL;
        }
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
        return getEstimatedSpeed(at, getInternalFixes());
    }
    
    @Override
    public synchronized SpeedWithBearing getRawEstimatedSpeed(TimePoint at) {
        return getEstimatedSpeed(at, getRawFixes());
    }

    /**
     * Since we don't know for sure whether the GPS fixes are {@link GPSFixMoving} instances, here we only estimate
     * speed based on the distance and time between the fixes, averaged over an interval of
     * {@link #millisecondsOverWhichToAverage} milliseconds around <code>at</code>. Subclasses that know about the
     * particular fix type may redefine this to exploit a {@link SpeedWithBearing} attached, e.g., to a
     * {@link GPSFixMoving}.
     */
    protected SpeedWithBearing getEstimatedSpeed(TimePoint at, NavigableSet<FixType> fixesToUseForSpeedEstimation) {
        @SuppressWarnings("unchecked")
        NavigableSet<GPSFix> gpsFixesToUseForSpeedEstimation = (NavigableSet<GPSFix>) fixesToUseForSpeedEstimation;
        List<GPSFix> relevantFixes = getFixesRelevantForSpeedEstimation(at, gpsFixesToUseForSpeedEstimation);
        double knotSum = 0;
        BearingCluster bearingCluster = new BearingCluster();
        int count = 0;
        if (!relevantFixes.isEmpty()) {
            Iterator<GPSFix> fixIter = relevantFixes.iterator();
            GPSFix last = fixIter.next();
            while (fixIter.hasNext()) {
                GPSFix next = fixIter.next();
                knotSum += last.getPosition().getDistance(next.getPosition())
                        .inTime(next.getTimePoint().asMillis() - last.getTimePoint().asMillis()).getKnots();
                bearingCluster.add(last.getPosition().getBearingGreatCircle(next.getPosition()));
                count++;
                last = next;
            }
        }
        SpeedWithBearing avgSpeed = new KnotSpeedWithBearingImpl(knotSum / count, bearingCluster.getAverage());
        return avgSpeed;
    }

    private List<GPSFix> getFixesRelevantForSpeedEstimation(TimePoint at,
            NavigableSet<GPSFix> fixesToUseForSpeedEstimation) {
        DummyGPSFix atTimed = new DummyGPSFix(at);
        List<GPSFix> relevantFixes = new LinkedList<GPSFix>();
        synchronized (this) {
            NavigableSet<GPSFix> beforeSet = fixesToUseForSpeedEstimation.headSet(atTimed, /* inclusive */ false);
            NavigableSet<GPSFix> afterSet = fixesToUseForSpeedEstimation.tailSet(atTimed, /* inclusive */ true);
            for (GPSFix beforeFix : beforeSet.descendingSet()) {
                if (at.asMillis() - beforeFix.getTimePoint().asMillis() > getMillisecondsOverWhichToAverage() / 2) {
                    break;
                }
                relevantFixes.add(0, beforeFix);
            }
            for (GPSFix afterFix : afterSet) {
                if (afterFix.getTimePoint().asMillis() - at.asMillis() > getMillisecondsOverWhichToAverage() / 2) {
                    break;
                }
                relevantFixes.add(afterFix);
            }
        }
        return relevantFixes;
    }

    protected long getMillisecondsOverWhichToAverage() {
        return millisecondsOverWhichToAverage;
    }

    /**
     * Smoothens the track based on a max-speed assumption.
     */
    @Override
    protected NavigableSet<FixType> getInternalFixes() {
        return new PartialNavigableSetView<FixType>(super.getInternalFixes()) {
            @Override
            protected boolean isValid(FixType e) {
                return GPSFixTrackImpl.this.isValid(this, e);
            }
        };
    }

    /**
     * When redefining this method, make sure to redefine {@link #invalidateValidityCaches(GPSFix)} accordingly.
     * This implementation checks the immediate previous and next fix for <code>e</code>. Therefore, when
     * adding a fix, only immediately adjacent fix's validity caches need to be invalidated.
     */
    protected boolean isValid(PartialNavigableSetView<FixType> filteredView, FixType e) {
        boolean result;
        if (maxSpeedForSmoothening == null) {
            result = true;
        } else {
            if (e.isValidityCached()) {
                result = e.isValid();
            } else {
                FixType previous = filteredView.lowerInternal(e);
                FixType next = filteredView.higherInternal(e);
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
                result = ((previous == null || speedToPrevious.compareTo(maxSpeedForSmoothening) <= 0) || (next == null || speedToNext
                        .compareTo(maxSpeedForSmoothening) <= 0));
                e.cacheValidity(result);
            }
        }
        return result;
    }

    /**
     * After <code>gpsFix</code> was added to this track, invalidate the {@link WithValidityCache validity caches}
     * of the fixes whose validity may be affected. If subclasses redefine {@link #isValid(PartialNavigableSetView, GPSFix)},
     * they must make sure that this method is redefined accordingly.
     */
    protected synchronized void invalidateValidityCaches(FixType gpsFix) {
        gpsFix.invalidateCache();
        FixType lower = getInternalRawFixes().lower(gpsFix);
        if (lower != null) {
            lower.invalidateCache();
        }
        FixType higher = getInternalRawFixes().higher(gpsFix);
        if (higher != null) {
            higher.invalidateCache();
        }
    }

    @Override
    public boolean hasDirectionChange(TimePoint at, double minimumDegreeDifference) {
        TimePoint start = new MillisecondsTimePoint(at.asMillis()-getMillisecondsOverWhichToAverageSpeed());
        TimePoint end = new MillisecondsTimePoint(at.asMillis()+getMillisecondsOverWhichToAverageSpeed());
        Bearing bearingAtStart = getEstimatedSpeed(start).getBearing();
        Bearing bearingAtEnd = getEstimatedSpeed(end).getBearing();
        // TODO also need to analyze the (smoothened) directions in between; example: two tacks within averaging interval
        return Math.abs(bearingAtStart.getDifferenceTo(bearingAtEnd).getDegrees()) > minimumDegreeDifference;
    }

}
