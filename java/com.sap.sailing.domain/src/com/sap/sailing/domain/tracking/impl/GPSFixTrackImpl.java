package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;

import com.sap.sailing.domain.base.BearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.base.impl.BearingWithConfidenceImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.SpeedWithBearingWithConfidenceImpl;
import com.sap.sailing.domain.base.impl.SpeedWithConfidenceImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.NauticalMileDistance;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.ConfidenceBasedAverager;
import com.sap.sailing.domain.confidence.ConfidenceFactory;
import com.sap.sailing.domain.confidence.HasConfidence;
import com.sap.sailing.domain.confidence.Weigher;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.GPSTrackListener;
import com.sap.sailing.domain.tracking.WithValidityCache;

public class GPSFixTrackImpl<ItemType, FixType extends GPSFix> extends TrackImpl<FixType> implements GPSFixTrack<ItemType, FixType> {
    private static final long serialVersionUID = -7282869695818293745L;
    private static final Speed DEFAULT_MAX_SPEED_FOR_SMOOTHING = new KnotSpeedImpl(50);
    protected final Speed maxSpeedForSmoothening;
    
    private final ItemType trackedItem;
    private long millisecondsOverWhichToAverage;

    private final Set<GPSTrackListener<ItemType, FixType>> listeners;
    
    public GPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        this(trackedItem, millisecondsOverWhichToAverage, DEFAULT_MAX_SPEED_FOR_SMOOTHING);
    }
    
    public GPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage, Speed maxSpeedForSmoothening) {
        super();
        this.trackedItem = trackedItem;
        this.millisecondsOverWhichToAverage = millisecondsOverWhichToAverage;
        this.maxSpeedForSmoothening = maxSpeedForSmoothening;
        this.listeners = new HashSet<GPSTrackListener<ItemType, FixType>>();
    }

    @Override
    public void addListener(GPSTrackListener<ItemType, FixType> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    @Override
    public void removeListener(GPSTrackListener<ItemType, FixType> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    /**
     * To iterate over the resulting listener list, synchronize on the iterable returned. Only this will avoid
     * {@link ConcurrentModificationException}s because listeners may be added on the fly, and this object will
     * synchronize on the listeners collection before adding on.
     */
    protected Iterable<GPSTrackListener<ItemType, FixType>> getListeners() {
        return listeners;
    }

    private class DummyGPSFix extends DummyTimed implements GPSFix {
        private static final long serialVersionUID = -6258506654181816698L;

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
    
    
    @Override
    protected FixType getDummyFix(TimePoint timePoint) {
        @SuppressWarnings("unchecked")
        FixType result = (FixType) new DummyGPSFix(timePoint);
        return result;
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

    private Pair<FixType, FixType> getFixesForPositionEstimation(TimePoint timePoint, boolean inclusive) {
        lockForRead();
        try {
            FixType lastFixBefore = inclusive ? getLastFixAtOrBefore(timePoint) : getLastFixBefore(timePoint);
            FixType firstFixAfter = inclusive ? getFirstFixAtOrAfter(timePoint) : getFirstFixAfter(timePoint);
            return new Pair<FixType, FixType>(lastFixBefore, firstFixAfter);
        } finally {
            unlockAfterRead();
        }
    }
    
    @Override
    public Position getEstimatedPosition(TimePoint timePoint, boolean extrapolate) {
        lockForRead();
        try {
            Pair<FixType, FixType> fixesForPositionEstimation = getFixesForPositionEstimation(timePoint, /* inclusive */ true);
            return getEstimatedPosition(timePoint, extrapolate, fixesForPositionEstimation.getA(), fixesForPositionEstimation.getB());
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public Pair<TimePoint, TimePoint> getEstimatedPositionTimePeriodAffectedBy(GPSFix fix) {
        lockForRead();
        try {
            Pair<FixType, FixType> fixesForPositionEstimation = getFixesForPositionEstimation(fix.getTimePoint(), /* inclusive */ false);
            return new Pair<TimePoint, TimePoint>(fixesForPositionEstimation.getA() == null ? fix.getTimePoint()
                    : fixesForPositionEstimation.getA().getTimePoint(),
                    fixesForPositionEstimation.getB() == null ? fix.getTimePoint() : fixesForPositionEstimation.getB().getTimePoint());
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public Position getEstimatedRawPosition(TimePoint timePoint, boolean extrapolate) {
        lockForRead();
        try {
            FixType lastFixAtOrBefore = getLastRawFixAtOrBefore(timePoint);
            FixType firstFixAtOrAfter = getFirstRawFixAtOrAfter(timePoint);
            return getEstimatedPosition(timePoint, extrapolate, lastFixAtOrBefore, firstFixAtOrAfter);
        } finally {
            unlockAfterRead();
        }
    }

    private Position getEstimatedPosition(TimePoint timePoint, boolean extrapolate, FixType lastFixAtOrBefore,
            FixType firstFixAtOrAfter) {
        lockForRead();
        try {
            // TODO bug #346: compute a confidence value for the position returned based on time difference between fix(es) and timePoint; consider using Taylor approximation of more fixes around timePoint to predict and weigh position
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
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public Speed getMaximumSpeedOverGround(TimePoint from, TimePoint to) {
        lockForRead();
        try {
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
        } finally {
            unlockAfterRead();
        }
    }

    protected Speed getSpeed(FixType fix, Position lastPos, TimePoint timePointOfLastPos) {
        lockForRead();
        try {
            return lastPos.getDistance(fix.getPosition()).inTime(fix.getTimePoint().asMillis()-timePointOfLastPos.asMillis());
        } finally {
            unlockAfterRead();
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
        lockForRead();
        try {
            double distanceInNauticalMiles = 0;
            if (from.compareTo(to) < 0) {
                Position fromPos = getEstimatedPosition(from, false);
                if (fromPos == null) {
                    return Distance.NULL;
                }
                lockForRead();
                try {
                    NavigableSet<GPSFix> subset = getGPSFixes().subSet(new DummyGPSFix(from),
                    /* fromInclusive */false, new DummyGPSFix(to),
                    /* toInclusive */false);
                    for (GPSFix fix : subset) {
                        double distanceBetweenAdjacentFixesInNauticalMiles = fromPos.getDistance(fix.getPosition()).getNauticalMiles();
                        distanceInNauticalMiles += distanceBetweenAdjacentFixesInNauticalMiles;
                        fromPos = fix.getPosition();
                    }
                } finally {
                    unlockAfterRead();
                }
                Position toPos = getEstimatedPosition(to, false);
                distanceInNauticalMiles += fromPos.getDistance(toPos).getNauticalMiles();
                return new NauticalMileDistance(distanceInNauticalMiles);
            } else {
                return Distance.NULL;
            }
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public Distance getRawDistanceTraveled(TimePoint from, TimePoint to) {
        lockForRead();
        try {
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
        } finally {
            unlockAfterRead();
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
    public SpeedWithBearing getEstimatedSpeed(TimePoint at) {
        lockForRead();
        try {
            SpeedWithBearingWithConfidence<TimePoint> estimatedSpeed = getEstimatedSpeed(at, getInternalFixes(),
                    ConfidenceFactory.INSTANCE.createExponentialTimeDifferenceWeigher(
                    // use a minimum confidence to avoid the bearing to flip to 270deg in case all is zero
                            getMillisecondsOverWhichToAverageSpeed()));
            return estimatedSpeed == null ? null : estimatedSpeed.getObject();
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public SpeedWithBearing getRawEstimatedSpeed(TimePoint at) {
        lockForRead();
        try {
            return getEstimatedSpeed(at, getRawFixes(),
                    ConfidenceFactory.INSTANCE.createExponentialTimeDifferenceWeigher(
                    // use a minimum confidence to avoid the bearing to flip to 270deg in case all is zero
                            getMillisecondsOverWhichToAverageSpeed())).getObject();
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public SpeedWithBearingWithConfidence<TimePoint> getEstimatedSpeed(TimePoint at, Weigher<TimePoint> weigher) {
        lockForRead();
        try {
            return getEstimatedSpeed(at, getInternalFixes(), weigher);
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public SpeedWithBearingWithConfidence<TimePoint> getRawEstimatedSpeed(TimePoint at, Weigher<TimePoint> weigher) {
        lockForRead();
        try {
            return getEstimatedSpeed(at, getRawFixes(), weigher);
        } finally {
            unlockAfterRead();
        }
    }

    /**
     * Since we don't know for sure whether the GPS fixes are {@link GPSFixMoving} instances, here we only estimate
     * speed based on the distance and time between the fixes, averaged over an interval of
     * {@link #millisecondsOverWhichToAverage} milliseconds around <code>at</code>. Subclasses that know about the
     * particular fix type may redefine this to exploit a {@link SpeedWithBearing} attached, e.g., to a
     * {@link GPSFixMoving}.
     * 
     * @param weigher
     *            If <code>null</code>, a confidence of 1.0 is assumed for all fixes used in the speed/bearing
     *            estimation. Otherwise, the confidence of each fix contributing to the estimation is computed using the
     *            <code>weigher</code>.
     * @return a speed/bearing with the average confidence attributed to the fixes that contributed to the estimation
     */
    protected SpeedWithBearingWithConfidence<TimePoint> getEstimatedSpeed(TimePoint at,
            NavigableSet<FixType> fixesToUseForSpeedEstimation, Weigher<TimePoint> weigher) {
        lockForRead();
        try {
            @SuppressWarnings("unchecked")
            NavigableSet<GPSFix> gpsFixesToUseForSpeedEstimation = (NavigableSet<GPSFix>) fixesToUseForSpeedEstimation;
            List<GPSFix> relevantFixes = getFixesRelevantForSpeedEstimation(at, gpsFixesToUseForSpeedEstimation);
            List<SpeedWithConfidence<TimePoint>> speeds = new ArrayList<SpeedWithConfidence<TimePoint>>();
            BearingWithConfidenceCluster<TimePoint> bearingCluster = new BearingWithConfidenceCluster<TimePoint>(weigher);
            if (!relevantFixes.isEmpty()) {
                Iterator<GPSFix> fixIter = relevantFixes.iterator();
                GPSFix last = fixIter.next();
                while (fixIter.hasNext()) {
                    // TODO bug #346: consider time difference between next.getTimepoint() and at to compute a confidence
                    GPSFix next = fixIter.next();
                    // TODO bug #345: use SpeedWithConfidence to aggregate confidence-tagged speed values
                    MillisecondsTimePoint relativeTo = new MillisecondsTimePoint((last.getTimePoint().asMillis() + next.getTimePoint().asMillis())/2);
                    Speed speed = last.getPosition().getDistance(next.getPosition())
                            .inTime(next.getTimePoint().asMillis() - last.getTimePoint().asMillis());
                    SpeedWithConfidenceImpl<TimePoint> speedWithConfidence = new SpeedWithConfidenceImpl<TimePoint>(speed, /* original confidence */
                            0.9, relativeTo);
                    speeds.add(speedWithConfidence);
                    bearingCluster.add(new BearingWithConfidenceImpl<TimePoint>(last.getPosition().getBearingGreatCircle(next.getPosition()),
                            /* confidence */ 0.9, // TODO use number of tracked satellites to determine confidence of single fix
                            relativeTo));
                    last = next;
                }
            }
            ConfidenceBasedAverager<Double, Speed, TimePoint> speedAverager = ConfidenceFactory.INSTANCE.createAverager(weigher);
            HasConfidence<Double, Speed, TimePoint> speedWithConfidence = speedAverager.getAverage(speeds, at);
            BearingWithConfidence<TimePoint> bearingAverage = bearingCluster.getAverage(at);
            Bearing bearing = bearingAverage == null ? null : bearingAverage.getObject();
            SpeedWithBearing avgSpeed = (speedWithConfidence == null || bearing == null) ? null :
                new KnotSpeedWithBearingImpl(speedWithConfidence.getObject().getKnots(), bearing);
            SpeedWithBearingWithConfidence<TimePoint> result = avgSpeed == null ? null :
                new SpeedWithBearingWithConfidenceImpl<TimePoint>(avgSpeed, (bearingAverage.getConfidence() + speedWithConfidence.getConfidence())/2., at);
            return result;
        } finally {
            unlockAfterRead();
        }
    }

    private List<GPSFix> getFixesRelevantForSpeedEstimation(TimePoint at,
            NavigableSet<GPSFix> fixesToUseForSpeedEstimation) {
        lockForRead();
        try {
            DummyGPSFix atTimed = new DummyGPSFix(at);
            List<GPSFix> relevantFixes = new LinkedList<GPSFix>();
            NavigableSet<GPSFix> beforeSet = fixesToUseForSpeedEstimation.headSet(atTimed, /* inclusive */false);
            NavigableSet<GPSFix> afterSet = fixesToUseForSpeedEstimation.tailSet(atTimed, /* inclusive */true);
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
            return relevantFixes;
        } finally {
            unlockAfterRead();
        }
    }

    protected long getMillisecondsOverWhichToAverage() {
        return millisecondsOverWhichToAverage;
    }

    /**
     * Smoothens the track based on a max-speed assumption.
     */
    @Override
    protected NavigableSet<FixType> getInternalFixes() {
        assertReadLock();
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
        assertReadLock();
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
    protected void invalidateValidityCaches(FixType gpsFix) {
        assertWriteLock();
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
        lockForRead();
        try {
            boolean result = false;
            TimePoint start = new MillisecondsTimePoint(at.asMillis() - getMillisecondsOverWhichToAverageSpeed());
            TimePoint end = new MillisecondsTimePoint(at.asMillis() + getMillisecondsOverWhichToAverageSpeed());
            SpeedWithBearing estimatedSpeedAtStart = getEstimatedSpeed(start);
            if (estimatedSpeedAtStart != null) {
                Bearing bearingAtStart = estimatedSpeedAtStart.getBearing();
                TimePoint next = new MillisecondsTimePoint(start.asMillis()
                        + Math.max(1000l, getMillisecondsOverWhichToAverageSpeed() / 2));
                while (!result && next.compareTo(end) <= 0) {
                    SpeedWithBearing estimatedSpeedAtNext = getEstimatedSpeed(next);
                    if (estimatedSpeedAtNext != null) {
                        Bearing bearingAtEnd = estimatedSpeedAtNext.getBearing();
                        result = Math.abs(bearingAtStart.getDifferenceTo(bearingAtEnd).getDegrees()) > minimumDegreeDifference;
                    }
                    next = new MillisecondsTimePoint(next.asMillis()
                            + Math.max(1000l, getMillisecondsOverWhichToAverageSpeed() / 2));
                }
            }
            return result;
        } finally {
            unlockAfterRead();
        }
    }

}
