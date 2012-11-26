package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
import com.sap.sailing.util.impl.ArrayListNavigableSet;

public class GPSFixTrackImpl<ItemType, FixType extends GPSFix> extends TrackImpl<FixType> implements GPSFixTrack<ItemType, FixType> {
    private static final long serialVersionUID = -7282869695818293745L;
    private static final Speed DEFAULT_MAX_SPEED_FOR_SMOOTHING = new KnotSpeedImpl(50);
    protected final Speed maxSpeedForSmoothing;
    
    private final ItemType trackedItem;
    private long millisecondsOverWhichToAverage;
    private final GPSTrackListeners<ItemType, FixType> listeners;
    

    private static class GPSTrackListeners<I, F extends GPSFix> implements Serializable {
        private static final long serialVersionUID = -7117842092078781722L;
        private Set<GPSTrackListener<I, F>> listeners;
        
        public GPSTrackListeners() {
            listeners = new HashSet<>();
        }
        
        @SuppressWarnings("unchecked") // need typed generic cast
        private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
            listeners = (Set<GPSTrackListener<I, F>>) ois.readObject();
        }
        
        private void writeObject(ObjectOutputStream oos) throws IOException {
            final Set<GPSTrackListener<I, F>> listenersToSerialize;
            synchronized (listeners) {
                listenersToSerialize = new HashSet<>();
                for (GPSTrackListener<I, F> listener : listeners) {
                    if (!listener.isTransient()) {
                        listenersToSerialize.add(listener);
                    }
                }
            }
            oos.writeObject(listenersToSerialize);
        }

        public void addListener(GPSTrackListener<I, F> listener) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
        
        public void removeListener(GPSTrackListener<I, F> listener) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
        
        /**
         * To iterate over the resulting listener list, synchronize on the iterable returned. Only this will avoid
         * {@link ConcurrentModificationException}s because listeners may be added on the fly, and this object will
         * synchronize on the listeners collection before adding on.
         */
        public Iterable<GPSTrackListener<I, F>> getListeners() {
            synchronized (listeners) {
                return new HashSet<GPSTrackListener<I, F>>(listeners);
            }
        }
    }
    
    /**
     * Computing {@link #getDistanceTraveled(TimePoint, TimePoint)} is more expensive the longer the track is and the
     * higher the tracking frequency. It is called regularly, usually for the same start time. Keeping to re-evaluate
     * all distances between all GPS fixes, even if they remain constant, is a waste of resources, particularly when we
     * assume that usually GPS fixes are appended to a track and not usually randomly inserted, even though this can
     * happen.
     * <p>
     * 
     * This cache looks "backwards." It contains pairs whose first component represents a <code>to</code> parameter used
     * in {@link #getDistanceTraveled(TimePoint, TimePoint)}. It is ordered by this component. The second component is a
     * navigable, ordered set of pairs where the first pair component represents a <code>from</code> parameter used in
     * {@link #getDistanceTraveled(TimePoint, TimePoint)} and the second pair component represents the result of
     * {@link #getDistanceTraveled(TimePoint, TimePoint)} for this parameter combination. Note that the cache does only
     * store distances for <code>from</code> time points actually passed to
     * {@link #getDistanceTraveled(TimePoint, TimePoint)} and does not cache all interim combinations collected while
     * computing a result. As such, it differs from a typical "dynamic programming" algorithm which would store all
     * in-between values too. Assuming that <code>from</code> values are sparse (leg start, race start, ...), there
     * doesn't seem to be much use in storing all the other in-between values too.
     * <p>
     * 
     * For implementation efficiency in combination with using a {@link ArrayListNavigableSet} for the values and in
     * order to be able to efficiently extend a cache entry for a single <code>to</code> fix, the navigable sets
     * containing the <code>from</code> fixes and distances are ordered such that earlier fixes come later in the set.
     * This way, extending the cache entry for a <code>to</code> fix to an earlier <code>from</code> fix only requires
     * appending to the set.
     * <p>
     * 
     * <b>Invalidation</b>: When a new fix is added to this track, all distance cache entries for fixes at or later than
     * the new fix's time point are removed from this cache. Additionally, the fix insertion may have an impact on the
     * {@link #getEarlierFixesWhoseValidityMayBeAffected(GPSFix) previous fix's} validity (track smoothing) and
     * therefore on its selection for distance aggregation. Therefore, if fix addition turned the previous fix invalid,
     * the cache entries for the time points at or after the previous fix also need to be removed.
     * <p>
     * 
     * <b>Cache use</b>: When {@link #getDistanceTraveled(TimePoint, TimePoint)} is called, it first looks for a cache
     * entry for the <code>to</code> parameter. If one is found, the earliest entry in the navigable set for the
     * navigable set of <code>from</code> and distance values that is at or after the requested <code>from</code> time
     * point is determined. If such an entry exists, the distance is remembered and the algorithm is repeated
     * recursively, using the <code>from</code> value found in the cache as the new <code>to</code> value, and the
     * <code>from</code> value originally passed to {@link #getDistanceTraveled(TimePoint, TimePoint)} as
     * <code>from</code> again. If no entry is found in the cache entry for <code>to</code> that is at or after the
     * requested <code>from</code> time, the distance is computed by iterating the smoothened fixes.
     * <p>
     * 
     * If a cache entry for <code>to</code> is not found, the latest cache entry before it is looked up. If one is
     * found, the distance between the <code>to</code> time point requested and the <code>to</code> time point found in
     * the cache is computed by iterating the smoothened fixes for this interval. If none is found, the distance is
     * computed by iterating backwards all the way to <code>from</code>.
     * <p>
     * 
     * Once the {@link #getDistanceTraveled(TimePoint, TimePoint)} has computed its value, it adds the result to the
     * cache.
     */
    private transient DistanceCache distanceCache;
    
    private transient MaxSpeedCache<ItemType, FixType> maxSpeedCache;
    
    public GPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage) {
        this(trackedItem, millisecondsOverWhichToAverage, DEFAULT_MAX_SPEED_FOR_SMOOTHING);
    }
    
    public GPSFixTrackImpl(ItemType trackedItem, long millisecondsOverWhichToAverage, Speed maxSpeedForSmoothening) {
        super(/* nameForReadWriteLock */ GPSFixTrackImpl.class.getSimpleName()+(trackedItem==null?"":(" for "+trackedItem.toString())));
        this.trackedItem = trackedItem;
        this.millisecondsOverWhichToAverage = millisecondsOverWhichToAverage;
        this.maxSpeedForSmoothing = maxSpeedForSmoothening;
        this.listeners = new GPSTrackListeners<>();
        this.distanceCache = new DistanceCache(trackedItem==null?"null":trackedItem.toString());
        this.maxSpeedCache = new MaxSpeedCache<>(this);
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        distanceCache = new DistanceCache(trackedItem.toString());
        maxSpeedCache = new MaxSpeedCache<ItemType, FixType>(this);
    }

    @Override
    public void addListener(GPSTrackListener<ItemType, FixType> listener) {
        listeners.addListener(listener);
    }
    
    @Override
    public void removeListener(GPSTrackListener<ItemType, FixType> listener) {
        listeners.removeListener(listener);
    }
    
    protected Iterable<GPSTrackListener<ItemType, FixType>> getListeners() {
        return listeners.getListeners();
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
            Pair<FixType, FixType> fixesForPositionEstimation = getFixesForPositionEstimation(fix.getTimePoint(), /* inclusive */ true);
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
    public Pair<FixType, Speed> getMaximumSpeedOverGround(TimePoint from, TimePoint to) {
        return maxSpeedCache.getMaxSpeed(from, to);
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
        return getDistanceTraveledRecursively(from, to, 0);
    }
    
    private Distance getDistanceTraveledRecursively(TimePoint from, TimePoint to, int recursionDepth) {
        Distance result;
        if (!from.before(to)) {
            result = Distance.NULL;
        } else {
            lockForRead();
            try {
                Pair<TimePoint, Pair<TimePoint, Distance>> bestCacheEntry = getDistanceCache()
                        .getEarliestFromAndDistanceAtOrAfterFrom(from, to);
                if (bestCacheEntry != null) {
                    // compute the missing stretches between best cache entry's "from" and our "from" and the cache
                    // entry's "to" and our "to"
                    Distance distanceFromFromToBeginningOfCacheEntry = Distance.NULL;
                    Distance distanceFromEndOfCacheEntryToTo = Distance.NULL;
                    if (!bestCacheEntry.getB().getA().equals(from)) {
                        assert bestCacheEntry.getB().getA().after(from);
                        distanceFromFromToBeginningOfCacheEntry = getDistanceTraveledRecursively(from, bestCacheEntry
                                .getB().getA(), recursionDepth + 1);
                    }
                    if (!bestCacheEntry.getA().equals(to)) {
                        assert bestCacheEntry.getA().before(to);
                        distanceFromEndOfCacheEntryToTo = getDistanceTraveledRecursively(bestCacheEntry.getA(), to,
                                recursionDepth + 1);
                    }
                    result = distanceFromFromToBeginningOfCacheEntry.add(bestCacheEntry.getB().getB()).add(
                            distanceFromEndOfCacheEntryToTo);
                } else {
                    double distanceInNauticalMiles = 0;
                    if (from.compareTo(to) < 0) {
                        // getEstimatedPosition's current implementation returns a position equal to that of a fix at
                        // "from" if there is one with exactly that time stamp
                        Position fromPos = getEstimatedPosition(from, /* extrapolate */false);
                        if (fromPos == null) {
                            result = Distance.NULL;
                        } else {
                            lockForRead();
                            try {
                                NavigableSet<GPSFix> subset = getGPSFixes().subSet(new DummyGPSFix(from),
                                /* fromInclusive */false, new DummyGPSFix(to),
                                /* toInclusive */false);
                                for (GPSFix fix : subset) {
                                    double distanceBetweenAdjacentFixesInNauticalMiles = fromPos.getDistance(
                                            fix.getPosition()).getNauticalMiles();
                                    distanceInNauticalMiles += distanceBetweenAdjacentFixesInNauticalMiles;
                                    fromPos = fix.getPosition();
                                }
                            } finally {
                                unlockAfterRead();
                            }
                            Position toPos = getEstimatedPosition(to, false);
                            distanceInNauticalMiles += fromPos.getDistance(toPos).getNauticalMiles();
                            result = new NauticalMileDistance(distanceInNauticalMiles);
                        }
                    } else {
                        result = Distance.NULL;
                    }
                }
            } finally {
                unlockAfterRead();
            }
            getDistanceCache().cache(from, to, result);
        }
        return result;
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
            GPSFix[] relevantFixes = getFixesRelevantForSpeedEstimation(at, fixesToUseForSpeedEstimation);
            List<SpeedWithConfidence<TimePoint>> speeds = new ArrayList<SpeedWithConfidence<TimePoint>>();
            BearingWithConfidenceCluster<TimePoint> bearingCluster = new BearingWithConfidenceCluster<TimePoint>(weigher);
            if (relevantFixes.length != 0) {
                int i=0;
                GPSFix last = relevantFixes[i];
                while (i<relevantFixes.length-1) {
                    // TODO bug #346: consider time difference between next.getTimepoint() and at to compute a confidence
                    GPSFix next = relevantFixes[++i];
                    aggregateSpeedAndBearingFromLastToNext(speeds, bearingCluster, last, next);
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

    protected void aggregateSpeedAndBearingFromLastToNext(List<SpeedWithConfidence<TimePoint>> speeds,
            BearingWithConfidenceCluster<TimePoint> bearingCluster, GPSFix last, GPSFix next) {
        MillisecondsTimePoint relativeTo = new MillisecondsTimePoint((last.getTimePoint().asMillis() + next.getTimePoint().asMillis())/2);
        Speed speed = last.getPosition().getDistance(next.getPosition())
                .inTime(next.getTimePoint().asMillis() - last.getTimePoint().asMillis());
        SpeedWithConfidenceImpl<TimePoint> speedWithConfidence = new SpeedWithConfidenceImpl<TimePoint>(speed, /* original confidence */
                0.9, relativeTo);
        speeds.add(speedWithConfidence);
        bearingCluster.add(new BearingWithConfidenceImpl<TimePoint>(last.getPosition().getBearingGreatCircle(next.getPosition()),
                /* confidence */ 0.9, // TODO use number of tracked satellites to determine confidence of single fix
                relativeTo));
    }

    /**
     * Computes the time interval such that {@link #getFixesRelevantForSpeedEstimation(TimePoint, NavigableSet)}, when
     * passed any time point from that interval, produces "fix" as part of its result array. The algorithm for
     * determining this interval co-varies with the implementation of
     * {@link #getFixesRelevantForSpeedEstimation(TimePoint, NavigableSet)}.<p>
     * 
     * This implementation looks for fixes {@link #getMillisecondsOverWhichToAverage()}/2 before and after <code>fix</code>.
     * If no fix is found in a direction within half the averaging interval but there is a fix in that direction which is further
     * apart, the yet next fix is checked. If there is none, or that next-next fix is further apart from the next fix than <code>fix</code>,
     * then the next fix is added to the result interval. This is because for that next fix, <code>fix</code> in that case will be relevant
     * for speed estimation because it's the closest fix.
     */
    protected Pair<TimePoint, TimePoint> getTimeIntervalWhoseEstimatedSpeedMayHaveChangedAfterAddingFix(FixType fix) {
        TimePoint intervalStart = null;
        TimePoint intervalEnd = null;
        lockForRead();
        try {
            NavigableSet<FixType> beforeSet = getInternalRawFixes().headSet(fix, /* inclusive */false);
            NavigableSet<FixType> afterSet = getInternalRawFixes().tailSet(fix, /* inclusive */true);
            FixType beforeFix = null;
            Iterator<FixType> beforeFixIter = beforeSet.descendingIterator();
            while (beforeFixIter.hasNext()
                    && fix.getTimePoint().asMillis() - (beforeFix = beforeFixIter.next()).getTimePoint().asMillis() < getMillisecondsOverWhichToAverage() / 2) {
                intervalStart = beforeFix.getTimePoint();
            }
            if (intervalStart == null) {
                if (beforeFixIter.hasNext()) {
                    // No before fix within half averaging interval, but there is one further away; is its next neighbour even further away?
                    // If so, or no more neighbours are found, it's affected and marks the invalidation interval start; otherwise, fix'
                    // time point is the invalidation interval start
                    TimePoint intervalStartCandidate = beforeFixIter.next().getTimePoint();
                    if (beforeFixIter.hasNext()) {
                        TimePoint nextNeighboursTimePoint = beforeFixIter.next().getTimePoint();
                        if (intervalStartCandidate.asMillis()-nextNeighboursTimePoint.asMillis() > fix.getTimePoint().asMillis()-intervalStartCandidate.asMillis()) {
                            intervalStart = intervalStartCandidate;
                        }
                    } else {
                        intervalStart = intervalStartCandidate;
                    }
                }
            }
            if (intervalStart == null) {
                intervalStart = fix.getTimePoint();
            }
            FixType afterFix = null;
            Iterator<FixType> afterFixIter = afterSet.iterator();
            while (afterFixIter.hasNext()
                    && (afterFix = afterFixIter.next()).getTimePoint().asMillis() - fix.getTimePoint().asMillis() < getMillisecondsOverWhichToAverage() / 2) {
                intervalEnd = afterFix.getTimePoint();
            }
            if (intervalEnd == null) {
                if (afterFixIter.hasNext()) {
                    // No after fix within half averaging interval, but there is one further away; is its next neighbour even further away?
                    // If so, or no more neighbours are found, it's affected and marks the invalidation interval start; otherwise, fix's
                    // time point is the invalidation interval end
                    TimePoint intervalEndCandidate = afterFixIter.next().getTimePoint();
                    if (afterFixIter.hasNext()) {
                        TimePoint nextNeighboursTimePoint = afterFixIter.next().getTimePoint();
                        if (nextNeighboursTimePoint.asMillis()-intervalEndCandidate.asMillis() > intervalEndCandidate.asMillis()-fix.getTimePoint().asMillis()) {
                            intervalEnd = intervalEndCandidate;
                        }
                    } else {
                        intervalEnd = intervalEndCandidate;
                    }
                }
            }
            if (intervalEnd == null) {
                intervalEnd = fix.getTimePoint();
            }
        } finally {
            unlockAfterRead();
        }
        return new Pair<TimePoint, TimePoint>(intervalStart, intervalEnd);
    }
    
    /**
     * A track that doesn't have {@link GPSFixMoving} fixes and therefore needs to compute the speed using the fix time
     * and position differences needs at least two fixes to compute a result. If only one fix is found within the
     * {@link #getMillisecondsOverWhichToAverage() averaging interval}, the closest existing fix later or earlier than
     * <code>at</code> is used.
     */
    protected GPSFix[] getFixesRelevantForSpeedEstimation(TimePoint at, NavigableSet<FixType> fixesToUseForSpeedEstimation) {
        lockForRead();
        try {
            DummyGPSFix atTimed = new DummyGPSFix(at);
            List<GPSFix> relevantFixes = new LinkedList<GPSFix>();
            @SuppressWarnings("unchecked")
            NavigableSet<GPSFix> castFixesToUseForSpeedEstimation = (NavigableSet<GPSFix>) fixesToUseForSpeedEstimation;
            NavigableSet<GPSFix> beforeSet = castFixesToUseForSpeedEstimation.headSet(atTimed, /* inclusive */false);
            NavigableSet<GPSFix> afterSet = castFixesToUseForSpeedEstimation.tailSet(atTimed, /* inclusive */true);
            GPSFix beforeFix = null;
            Iterator<GPSFix> beforeFixIter = beforeSet.descendingIterator();
            while (beforeFixIter.hasNext() &&
                    at.asMillis() - (beforeFix=beforeFixIter.next()).getTimePoint().asMillis() < getMillisecondsOverWhichToAverage() / 2) {
                relevantFixes.add(0, beforeFix);
            }
            GPSFix afterFix = null;
            Iterator<GPSFix> afterFixIter = afterSet.iterator();
            while (afterFixIter.hasNext() &&
                    (afterFix=afterFixIter.next()).getTimePoint().asMillis() - at.asMillis() < getMillisecondsOverWhichToAverage() / 2) {
                relevantFixes.add(afterFix);
            }
            // now fill up relevantFixes until we have at least two fixes or we run out of fixes entirely (can't estimate speed
            // with this type of fix on a track with less than two fixes)
            while (relevantFixes.size() < 2 && (beforeFix != null || afterFix != null)) {
                if (afterFix == null) {
                    if (beforeFix != null) {
                        relevantFixes.add(0, beforeFix); // add the last beforeFix to have at least two fixes, although outside of averaging interval
                        if (beforeFixIter.hasNext()) {
                            beforeFix = beforeFixIter.next();
                        } else {
                            beforeFix = null;
                        }
                    }
                } else if (beforeFix == null) {
                    relevantFixes.add(afterFix);
                    if (afterFixIter.hasNext()) {
                        afterFix = afterFixIter.next();
                    } else {
                        afterFix = null;
                    }
                } else {
                    // both, beforeFix and afterFix are available; choose the one closest to "at"
                    if (afterFix.getTimePoint().asMillis()-at.asMillis() < beforeFix.getTimePoint().asMillis()-at.asMillis()) {
                        relevantFixes.add(afterFix);
                        if (afterFixIter.hasNext()) {
                            afterFix = afterFixIter.next();
                        } else {
                            afterFix = null;
                        }
                    } else {
                        relevantFixes.add(0, beforeFix);
                        if (beforeFixIter.hasNext()) {
                            beforeFix = beforeFixIter.next();
                        } else {
                            beforeFix = null;
                        }
                    }
                }
            }
            return relevantFixes.toArray(new GPSFix[0]);
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
                return GPSFixTrackImpl.this.isValid(getRawFixes(), e);
            }
        };
    }

    /**
     * When redefining this method, make sure to redefine {@link #invalidateValidityAndDistanceCaches(GPSFix)} accordingly.
     * This implementation checks the immediate previous and next fix for <code>e</code>. Therefore, when
     * adding a fix, only immediately adjacent fix's validity caches need to be invalidated.
     */
    protected boolean isValid(NavigableSet<FixType> rawFixes, FixType e) {
        assertReadLock();
        boolean result;
        if (maxSpeedForSmoothing == null) {
            result = true;
        } else {
            if (e.isValidityCached()) {
                result = e.isValid();
            } else {
                FixType previous = rawFixes.lower(e);
                FixType next = rawFixes.higher(e);
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
                result = ((previous == null || speedToPrevious.compareTo(maxSpeedForSmoothing) <= 0) || (next == null || speedToNext
                        .compareTo(maxSpeedForSmoothing) <= 0));
                e.cacheValidity(result);
            }
        }
        return result;
    }

    /**
     * After <code>gpsFix</code> was added to this track, invalidate the {@link WithValidityCache validity caches}
     * of the fixes whose validity may be affected. If subclasses redefine {@link #isValid(PartialNavigableSetView, GPSFix)},
     * they must make sure that this method is redefined accordingly.<p>
     * 
     * Distance cache invalidation is a bit tricky. Usually, the distance cache is invalidated starting with the time point
     * of the <code>gpsFix</code> "upwards." However, if the adjacent earlier fixes have changed their validity by the addition
     * of <code>gpsFix</code>, the distance cache must be invalidated starting with the first fix whose validity changed.
     */
    protected void invalidateValidityAndDistanceCaches(FixType gpsFix) {
        assertWriteLock();
        TimePoint distanceCacheInvalidationStart = gpsFix.getTimePoint();
        // see also bug 968: cache entries for intervals ending after the last fix need to be removed because they are
        // based on the last fix's position and don't extrapolate; now, with the new gpsFix, the positions between the last
        // and the new fix are defined by interpolation and hence differ from the previous assumption the competitor would have
        // stopped moving at the last fix.
        FixType last = getInternalFixes().lower(gpsFix);
        if (last != null) {
            distanceCacheInvalidationStart = last.getTimePoint().plus(1); // add one millisecond to invalidate *after* the last fix only
        }
        gpsFix.invalidateCache();
        Iterable<FixType> lowers = getEarlierFixesWhoseValidityMayBeAffected(gpsFix);
        for (FixType lower : lowers) {
            boolean lowerWasValid = isValid(getRawFixes(), lower);
            lower.invalidateCache();
            boolean lowerIsValid = isValid(getRawFixes(), lower);
            if (lowerIsValid != lowerWasValid && lower.getTimePoint().before(distanceCacheInvalidationStart)) {
                distanceCacheInvalidationStart = lower.getTimePoint();
            }
        }
        getDistanceCache().invalidateAllAtOrLaterThan(distanceCacheInvalidationStart);
        Iterable<FixType> highers = getLaterFixesWhoseValidityMayBeAffected(gpsFix);
        for (FixType higher : highers) {
            higher.invalidateCache();
        }
    }

    protected Iterable<FixType> getLaterFixesWhoseValidityMayBeAffected(FixType gpsFix) {
        FixType higher = getInternalRawFixes().higher(gpsFix);
        if (higher == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(higher);
        }
    }

    protected Iterable<FixType> getEarlierFixesWhoseValidityMayBeAffected(FixType gpsFix) {
        FixType lower = getInternalRawFixes().lower(gpsFix);
        if (lower == null) {
            return Collections.emptySet();
        } else {
            return Collections.singleton(lower);
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

    protected DistanceCache getDistanceCache() {
        return distanceCache;
    }

}
