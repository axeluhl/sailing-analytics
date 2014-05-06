package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.AbstractTimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.SerializableComparator;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.ConfidenceFactory;
import com.sap.sailing.domain.confidence.Weigher;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.util.impl.ArrayListNavigableSet;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

/**
 * A virtual wind track that computes and caches the wind bearing based on the boat tracks recorded in the tracked race
 * for which this wind track is constructed. It has a fixed time resolution as defined by the constant
 * {@link #RESOLUTION_IN_MILLISECONDS}. When asked for the wind at a time at which the wind cannot be estimated, the raw
 * fixes will have <code>null</code> as value for this time. These <code>null</code> "fixes" are at the same time
 * considered "outliers" by the {@link #getInternalFixes()} operation which filters them from the "smoothened" view.
 * With this, the view with "outliers" removed contains all those fixes for which the wind bearing was successfully
 * computed from the tracked race's boat tracks.
 * <p>
 * 
 * The estimation is integrated into the {@link WindTrackImpl} concepts by redefining the {@link #getInternalRawFixes()}
 * method such that it returns an {@link EstimatedWindFixesAsNavigableSet} object. It computes its values by asking back
 * to {@link #getEstimatedWindDirection(Position, TimePoint)} which first performs a cache look-up. In case of a cache
 * miss it determines the result based on {@link TrackedRace#getEstimatedWindDirection(Position, TimePoint)}.
 * <p>
 * 
 * Caching is done using the base class's {@link TrackImpl#fixes} field which is made accessible through
 * {@link #getCachedFixes()}. This track observes the {@link TrackedRace} for which it provides wind estimations.
 * Whenever a change occurs, all fixes whose derivation is potentially affected by the change are removed from the
 * cache. For new GPS fixes arriving this is the time span used for
 * {@link TrackedRace#getMillisecondsOverWhichToAverageSpeed() averaging speeds}. For a new mark passing, all fixes
 * between the old and new mark passing times as well as those
 * {@link TrackedRace#getMillisecondsOverWhichToAverageSpeed()} before and after this time period are removed from the
 * cache. If the {@link #speedAveragingChanged(long, long) speed averaging changes}, the entire cache is cleared.<p>
 * 
 * Note the {@link #getMillisecondsOverWhichToAverageWind() reduced averaging interval} used by this track type.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class TrackBasedEstimationWindTrackImpl extends VirtualWindTrackImpl implements RaceChangeListener {
    private static final long serialVersionUID = -4397496421917807499L;

    private static final SpeedWithBearing defaultSpeedWithBearing = new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(0));

    private final EstimatedWindFixesAsNavigableSet virtualInternalRawFixes;

    private final NavigableSet<TimePoint> timePointsWithCachedNullResult;
    
    /**
     * Access to this cache is guarded by {@link #cacheLock}.
     */
    private final NavigableSet<WindWithConfidence<TimePoint>> cache;
    
    private final NamedReentrantReadWriteLock cacheLock;
    
    private final Weigher<TimePoint> weigher;
    
    /**
     * A copy of the {@link #timePointsWithCachedNullResult} contents offering fast contains checks.
     */
    private final HashSet<TimePoint> timePointsWithCachedNullResultFastContains;

    /**
     * When mark and boat position changes are received, they cause the cache to be invalidated a certain time interval
     * around the time point of the event. If the cache invalidation happens immediately, this can cause significant
     * load on the server. Delaying the cache refresh just a little will reduce server load, sacrificing some accuracy
     * of the wind estimation which can carefully be traded by this parameter.
     */
    private final long delayForCacheInvalidationInMilliseconds;
    
    private static class InvalidationInterval implements Serializable {
        private static final long serialVersionUID = -6406690520919193690L;
        private WindWithConfidence<TimePoint> start;
        private TimePoint end;
        public InvalidationInterval() {
            super();
        }
        public WindWithConfidence<TimePoint> getStart() {
            return start;
        }
        public TimePoint getEnd() {
            return end;
        }
        public synchronized void clear() {
            start = null;
            end = null;
        }
        public synchronized boolean isSet() {
            return start != null && end != null;
        }
        public synchronized void set(WindWithConfidence<TimePoint> startOfInvalidation, TimePoint endOfInvalidation) {
            this.start = startOfInvalidation;
            this.end = endOfInvalidation;
        }
        public synchronized void extend(WindWithConfidence<TimePoint> startOfInvalidation, TimePoint endOfInvalidation) {
            if (startOfInvalidation.getObject().getTimePoint().compareTo(start.getObject().getTimePoint()) < 0) {
                this.start = startOfInvalidation;
            }
            if (endOfInvalidation.compareTo(end) > 0) {
                end = endOfInvalidation;
            }
        }
    }
    
    /**
     * {@link #scheduleCacheRefresh(WindWithConfidence, TimePoint)} synchronizes on this object before changing it and
     * when actually invalidating the cache. When a querying thread holds at least the read lock of {@link #cacheLock}
     * and the refresh interval it {@link InvalidationInterval#isSet() is set}, an invalidation is currently running or
     * has been scheduled.
     */
    private final InvalidationInterval scheduledRefreshInterval;

    /**
     * @param delayForCacheInvalidationInMilliseconds
     *            When mark and boat position changes are received, they cause the cache to be invalidated a certain
     *            time interval around the time point of the event. If the cache invalidation happens immediately, this
     *            can cause significant load on the server. Delaying the cache refresh just a little will reduce server
     *            load, sacrificing some accuracy of the wind estimation which can carefully be traded by this
     *            parameter. When the delay is set to 0, the cache contents for the affected time interval are
     *            immediately removed and will be re-computed upon the next request. If a positive delay is specified,
     *            the cache contents for the interval affected will be re-computed and will be replaced in the cache
     *            when the new results are available. Clients therefore won't have to wait for the valued to be
     *            re-computed but will be served from the old cache values until they will have been replaced.
     */
    public TrackBasedEstimationWindTrackImpl(TrackedRace trackedRace, long millisecondsOverWhichToAverage,
            double baseConfidence, long delayForCacheInvalidationInMilliseconds) {
        super(trackedRace, millisecondsOverWhichToAverage, baseConfidence,
                WindSourceType.TRACK_BASED_ESTIMATION.useSpeed());
        this.delayForCacheInvalidationInMilliseconds = delayForCacheInvalidationInMilliseconds;
        this.scheduledRefreshInterval = new InvalidationInterval();
        cache = new ArrayListNavigableSet<WindWithConfidence<TimePoint>>(
                new SerializableComparator<WindWithConfidence<TimePoint>>() {
                    private static final long serialVersionUID = 5760349397418542705L;

                    @Override
                    public int compare(WindWithConfidence<TimePoint> o1, WindWithConfidence<TimePoint> o2) {
                        return o1.getObject().getTimePoint().compareTo(o2.getObject().getTimePoint());
                    }
                });
        cacheLock = new NamedReentrantReadWriteLock(TrackBasedEstimationWindTrackImpl.class.getSimpleName()+" for race "+
                trackedRace.getRace().getName(), /* fair */ false);
        virtualInternalRawFixes = new EstimatedWindFixesAsNavigableSet(trackedRace);
        weigher = ConfidenceFactory.INSTANCE
                .createHyperbolicTimeDifferenceWeigher(getMillisecondsOverWhichToAverageWind());
        trackedRace.addListener(this); // in particular, race status changes will be notified, unblocking waiting computations after LOADING phase
        this.timePointsWithCachedNullResult = new ArrayListNavigableSet<TimePoint>(
                AbstractTimePoint.TIMEPOINT_COMPARATOR);
        this.timePointsWithCachedNullResultFastContains = new HashSet<TimePoint>();
    }
    
    /**
     * Synchronizes serialization on this object to avoid the cache being updated while being written.
     */
    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        LockUtil.lockForRead(cacheLock);
        lockForRead();
        try {
            s.defaultWriteObject();
        } finally {
            unlockAfterRead();
            LockUtil.unlockAfterRead(cacheLock);
        }
    }
    
    /**
     * Constructs this track with cache invalidation happening after half the
     * {@link TrackedRace#getMillisecondsOverWhichToAverageWind() wind averaging interval specified by the tracked race}
     * . Good for test cases; shouldn't be use if you don't want to overload the server.
     */
    public TrackBasedEstimationWindTrackImpl(TrackedRace trackedRace, long millisecondsOverWhichToAverage,
            double baseConfidence) {
        this(trackedRace, millisecondsOverWhichToAverage, baseConfidence, /* delayForCacheInvalidationInMilliseconds */
                trackedRace.getMillisecondsOverWhichToAverageWind() / 2);
    }
    
    /**
     * The track-based estimation already averages the boats' bearings over time before averaging those across legs and
     * tacks. There is no use in again averaging over a longer period of time. Therefore, we set the averaging interval
     * to two times the resolution of the {@link EstimatedWindFixesAsNavigableSet virtual fixes collection} plus two
     * milliseconds, so that at most one fix before and one fix after the time point requested will be used.
     */
    @Override
    public long getMillisecondsOverWhichToAverageWind() {
        return 2*getInternalRawFixes().getResolutionInMilliseconds()+2;
    }
    
    @Override
    protected VirtualWindFixesAsNavigableSet getInternalRawFixes() {
        return virtualInternalRawFixes;
    }

    private NavigableSet<WindWithConfidence<TimePoint>> getCachedFixes() {
        return cache;
    }
    
    protected void cache(TimePoint timePoint, WindWithConfidence<TimePoint> fix) {
        // can't use lockForWrite() here because caching can happen while holding the read lock, and the lock can't be
        // upgraded. But lockForRead() and synchronization will do the job because all invalidations lock the write lock,
        // and all contains() checks and get() calls use synchronization too.
        LockUtil.lockForWrite(cacheLock);
        try {
            // synchronization necessary to protect writeObject from ConcurrentModificationException
            synchronized (this) {
                if (fix == null) {
                    synchronized (timePointsWithCachedNullResult) {
                        timePointsWithCachedNullResult.add(timePoint);
                    }
                    synchronized (timePointsWithCachedNullResultFastContains) {
                        timePointsWithCachedNullResultFastContains.add(timePoint);
                    }
                } else {
                    LockUtil.lockForWrite(cacheLock);
                    try {
                        cache.add(fix);
                    } finally {
                        LockUtil.unlockAfterWrite(cacheLock);
                    }
                }
            }
        } finally {
            LockUtil.unlockAfterWrite(cacheLock);
        }
    }
    
    /**
     * Schedules a cache invalidation for the time interval specified. The scheduling delay is configured during construction of
     * this track. The longer the scheduling delay, the less load this track will cause for the server because invalidations will
     * be bundled, and during live mode the incoming requests for a time point close to the time for which new data is received
     * will not be massively delayed by having to re-calculate the estimation over and over again.
     */
    private void scheduleCacheRefresh(WindWithConfidence<TimePoint> startOfInvalidation, TimePoint endOfInvalidation) {
        LockUtil.lockForWrite(cacheLock);
        try {
            if (!scheduledRefreshInterval.isSet()) {
                // according to the invariant this implies [1]==null
                scheduledRefreshInterval.set(startOfInvalidation, endOfInvalidation);
                startSchedulerForCacheRefresh();
            } else {
                // this means that an invalidation or incremental refresh is already scheduled; as long as we're
                // synchronized on scheduledInvalidationInterval we can safely extend the interval; the invalidation
                // won't start before we release the lock
                scheduledRefreshInterval.extend(startOfInvalidation, endOfInvalidation);
            }
        } finally {
            LockUtil.unlockAfterWrite(cacheLock);
        }
    }
    
    /**
     * Invalidates the cache based on {@link #scheduledRefreshInterval} and when done
     * {@link InvalidationInterval#clear() clears} the invalidation interval, indicating that currently no scheduler is
     * running.
     */
    private void invalidateCache() {
        LockUtil.lockForWrite(cacheLock);
        try {
            Iterator<WindWithConfidence<TimePoint>> iter = (scheduledRefreshInterval.getStart() == null ? getCachedFixes()
                    : getCachedFixes().tailSet(scheduledRefreshInterval.getStart(), /* inclusive */true)).iterator();
            while (iter.hasNext()) {
                WindWithConfidence<TimePoint> next = iter.next();
                if (scheduledRefreshInterval.getEnd() == null || next.getObject().getTimePoint().compareTo(scheduledRefreshInterval.getEnd()) < 0) {
                    iter.remove();
                } else {
                    break;
                }
            }
            Iterator<TimePoint> nullIter = (scheduledRefreshInterval.getStart() == null ? timePointsWithCachedNullResult
                    : timePointsWithCachedNullResult.tailSet(scheduledRefreshInterval.getStart().getObject().getTimePoint(), /* inclusive */
                    true)).iterator();
            while (nullIter.hasNext()) {
                TimePoint next = nullIter.next();
                if (scheduledRefreshInterval.getEnd() == null || next.compareTo(scheduledRefreshInterval.getEnd()) < 0) {
                    nullIter.remove();
                    timePointsWithCachedNullResultFastContains.remove(next);
                } else {
                    break;
                }
            }
            scheduledRefreshInterval.clear();
        } finally {
            LockUtil.unlockAfterWrite(cacheLock);
        }
    }

    /**
     * Incrementally replaces the cache elements based on {@link #scheduledRefreshInterval} using freshly computed values.
     */
    private void refreshCacheIncrementally() {
        Set<WindWithConfidence<TimePoint>> windFixesToRecalculate = new HashSet<WindWithConfidence<TimePoint>>();
        Set<TimePoint> cachedNullResultsToRecalculate = new HashSet<TimePoint>();
        LockUtil.lockForRead(cacheLock);
        try {
            Iterator<WindWithConfidence<TimePoint>> iter = (scheduledRefreshInterval.getStart() == null ? getCachedFixes()
                    : getCachedFixes().tailSet(scheduledRefreshInterval.getStart(), /* inclusive */true)).iterator();
            Iterator<TimePoint> nullIter = (scheduledRefreshInterval.getStart() == null ? timePointsWithCachedNullResult
                    : timePointsWithCachedNullResult.tailSet(scheduledRefreshInterval.getStart().getObject().getTimePoint(), /* inclusive */
                    true)).iterator();
            WindWithConfidence<TimePoint> nextFixToRecalculate = null;
            while (iter.hasNext() &&
                    ((nextFixToRecalculate = iter.next()).getObject().getTimePoint().compareTo(scheduledRefreshInterval.getEnd()) < 0) ||
                    scheduledRefreshInterval.getEnd() == null) {
                windFixesToRecalculate.add(nextFixToRecalculate);
            }
            TimePoint nextNullResultToRecalculate = null;
            while (nullIter.hasNext() &&
                    ((nextNullResultToRecalculate = nullIter.next()).compareTo(scheduledRefreshInterval.getEnd()) < 0) ||
                    scheduledRefreshInterval.getEnd() == null) {
                cachedNullResultsToRecalculate.add(nextNullResultToRecalculate);
            }
        } finally {
            LockUtil.unlockAfterRead(cacheLock);
        }
        Set<TimePoint> nullRemovals = new HashSet<TimePoint>();
        Set<TimePoint> nullInsertions = new HashSet<TimePoint>();
        Map<TimePoint, WindWithConfidence<TimePoint>> cacheInsertions = new HashMap<TimePoint, WindWithConfidence<TimePoint>>();
        for (TimePoint cachedNullResultToRecalculate : cachedNullResultsToRecalculate) {
            WindWithConfidence<TimePoint> replacementFix = getTrackedRace()
                    .getEstimatedWindDirectionWithConfidence(/* position */ null, cachedNullResultToRecalculate);
            if (replacementFix != null) {
                nullRemovals.add(cachedNullResultToRecalculate);
                cacheInsertions.put(cachedNullResultToRecalculate, replacementFix);
            } // else no action required because the result is still null
        }
        for (WindWithConfidence<TimePoint> windFixToRecalculate : windFixesToRecalculate) {
            TimePoint timePoint = windFixToRecalculate.getObject().getTimePoint();
            Position position = windFixToRecalculate.getObject().getPosition();
            WindWithConfidence<TimePoint> replacementFix = getTrackedRace()
                    .getEstimatedWindDirectionWithConfidence(position, timePoint);
            if (replacementFix == null) {
                nullInsertions.add(timePoint);
            } else {
                cacheInsertions.put(timePoint, replacementFix);
            }
        }
        // apply the computed cache deltas
        LockUtil.lockForWrite(cacheLock);
        try {
            for (TimePoint nullRemoval : nullRemovals) {
                timePointsWithCachedNullResult.remove(nullRemoval);
                timePointsWithCachedNullResultFastContains.remove(nullRemoval);
            }
            for (TimePoint nullInsertion : nullInsertions) {
                cache(nullInsertion, null);
            }
            for (WindWithConfidence<TimePoint> cacheRemoval : windFixesToRecalculate) {
                getCachedFixes().remove(cacheRemoval);
            }
            for (Map.Entry<TimePoint, WindWithConfidence<TimePoint>> cacheInsertion : cacheInsertions.entrySet()) {
                cache(cacheInsertion.getKey(), cacheInsertion.getValue());
            }
            scheduledRefreshInterval.clear();
        } finally {
            LockUtil.unlockAfterWrite(cacheLock);
        }
    }

    private void startSchedulerForCacheRefresh() {
        if (delayForCacheInvalidationInMilliseconds == 0) {
            invalidateCache();
        } else {
            final Timer cacheInvalidationTimer = new Timer(
                    "TrackBasedEstimationWindTrackImpl cache invalidation timer for race " + getTrackedRace().getRace());
            cacheInvalidationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // no locking required here; the incremental cache refresh protects the inner cache structures from concurrent modifications
                    cacheInvalidationTimer.cancel(); // terminates the timer thread
                    if (getTrackedRace().getStatus().getStatus() == TrackedRaceStatusEnum.LOADING) {
                        // during loading, only invalidate the cache after the interval expired but don't trigger incremental re-calculation
                        invalidateCache();
                    } else {
                        refreshCacheIncrementally();
                    }
                }
            }, delayForCacheInvalidationInMilliseconds);
        }
    }

    private void clearCache() {
        LockUtil.lockForWrite(cacheLock);
        try {
            cache.clear();
            timePointsWithCachedNullResult.clear();
            timePointsWithCachedNullResultFastContains.clear();
        } finally {
            LockUtil.unlockAfterWrite(cacheLock);
        }
    }

    /**
     * Looks up wind data in the {@link #getCachedFixes() cache} and the {@link #getTimePointsWithCachedNullResult()
     * null store} first. Only if nothing is found for the time point requested, the
     * {@link TrackedRace#getEstimatedWindDirection(Position, TimePoint) wind estimation algorithm} is used to compute
     * it. The result will then be added to the cache.
     */
    private WindWithConfidence<TimePoint> getEstimatedWindDirection(Position p, TimePoint timePoint) {
        WindWithConfidence<TimePoint> cachedFix = null;
        WindWithConfidence<TimePoint> result = null;
        final boolean nullResultCacheContains;
        LockUtil.lockForRead(cacheLock);
        try {
            nullResultCacheContains = nullResultCacheContains(timePoint);
            if (nullResultCacheContains) {
                result = null;
            } else {
                cachedFix = cache.floor(getDummyFixWithConfidence(timePoint));
            }
        } finally {
            LockUtil.unlockAfterRead(cacheLock);
        }
        if (!nullResultCacheContains) {
            if (cachedFix == null || !cachedFix.getObject().getTimePoint().equals(timePoint)) {
                result = getTrackedRace().getEstimatedWindDirectionWithConfidence(p, timePoint);
                cache(timePoint, result);
            } else {
                result = cachedFix;
            }
        }
        return result;
    }

    private WindWithConfidence<TimePoint> getDummyFixWithConfidence(TimePoint timePoint) {
        return new WindWithConfidenceImpl<TimePoint>(new WindImpl(null, timePoint, defaultSpeedWithBearing), 0,
                timePoint, /* useSpeed */false);
    }

    private boolean nullResultCacheContains(TimePoint timePoint) {
        assertReadLock();
        synchronized (timePointsWithCachedNullResultFastContains) {
            return timePointsWithCachedNullResultFastContains.contains(timePoint);
        }
    }

    @Override
    public void windDataReceived(Wind wind, WindSource windSource) {
        invalidateForNewWind(wind, windSource);
    }
    
    @Override
    public void raceTimesChanged(TimePoint startOfTracking, TimePoint endOfTracking, TimePoint startTimeReceived) {
    }

    @Override
    public void delayToLiveChanged(long delayToLiveInMillis) {
    }

    private void invalidateForNewWind(Wind wind, WindSource windSource) {
        WindTrack windTrack = getTrackedRace().getOrCreateWindTrack(windSource);
        // check what the next fixes before and after the one affected are; if they are further than the averagingInterval
        // away, extend the invalidation interval accordingly because the entire span up to the next fix may be influenced
        // by adding/removing a fix in a sparsely occupied track. See WindTrackImpl.getAveragedWindUnsynchronized(Position p, TimePoint at)
        long averagingInterval = getTrackedRace().getMillisecondsOverWhichToAverageWind();
        final TimePoint timePoint = wind.getTimePoint();
        // See WindComparator; if time is equal, position is compared; for dummy fixes this will create arbitrary order, so ensure time point cannot
        // accidentally be equal
        Wind lastFixBefore = windTrack.getLastFixBefore(timePoint.minus(1)); // subtract one millisecond to be sure to be before a fix just inserted
        Wind firstFixAfter = windTrack.getFirstFixAfter(timePoint.plus(1)); // add one millisecond to be sure to be after a fix just inserted
        final WindWithConfidence<TimePoint> startOfInvalidation;
        if (lastFixBefore == null) {
            startOfInvalidation = getTrackedRace().getStartOfTracking() == null ? getDummyFixWithConfidence(new MillisecondsTimePoint(0l))
                    : getDummyFixWithConfidence(getTrackedRace().getStartOfTracking());
        } else if (lastFixBefore.getTimePoint().before(timePoint.minus(averagingInterval))) {
            startOfInvalidation = new WindWithConfidenceImpl<TimePoint>(lastFixBefore, 1.0, timePoint, windSource.getType().useSpeed());
        } else {
            startOfInvalidation = getDummyFixWithConfidence(timePoint.minus(averagingInterval));
        }
        final TimePoint endOfInvalidation;
        if (firstFixAfter == null) {
            endOfInvalidation = getTrackedRace().getEndOfTracking() == null ? new MillisecondsTimePoint(Long.MAX_VALUE) : getTrackedRace().getEndOfTracking();
        } else if (firstFixAfter.getTimePoint().after(timePoint.plus(averagingInterval))) {
            endOfInvalidation = firstFixAfter.getTimePoint();
        } else {
            endOfInvalidation = timePoint.plus(averagingInterval);
        }
        scheduleCacheRefresh(startOfInvalidation, endOfInvalidation);
    }

    @Override
    public void windDataRemoved(Wind wind, WindSource windSource) {
        invalidateForNewWind(wind, windSource);
    }

    @Override
    public void windSourcesToExcludeChanged(Iterable<? extends WindSource> windSourcesToExclude) {
        clearCache();
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        clearCache();
    }

    @Override
    public void competitorPositionChanged(GPSFixMoving fix, Competitor competitor) {
        long averagingInterval = getTrackedRace().getMillisecondsOverWhichToAverageSpeed();
        WindWithConfidence<TimePoint> startOfInvalidation = getDummyFixWithConfidence(new MillisecondsTimePoint(fix
                .getTimePoint().asMillis() - averagingInterval));
        TimePoint endOfInvalidation = new MillisecondsTimePoint(fix.getTimePoint().asMillis() + averagingInterval);
        scheduleCacheRefresh(startOfInvalidation, endOfInvalidation);
    }
    
    @Override
    public void statusChanged(TrackedRaceStatus newStatus) {
        // This virtual wind track's cache can cope with an empty cache after the LOADING phase and populates the cache
        // upon request. Invalidation happens also during the LOADING phase, preserving the cache's invariant.
    }

    @Override
    public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings, Iterable<MarkPassing> markPassings) {
        long averagingInterval = getTrackedRace().getMillisecondsOverWhichToAverageSpeed();
        WindWithConfidence<TimePoint> startOfInvalidation;
        TimePoint endOfInvalidation;
        for (MarkPassing markPassing : markPassings) {
            MarkPassing oldMarkPassing = oldMarkPassings.get(markPassing.getWaypoint());
            if (oldMarkPassing != markPassing) {
                if (oldMarkPassing == null) {
                    startOfInvalidation = getDummyFixWithConfidence(new MillisecondsTimePoint(markPassing
                            .getTimePoint().asMillis() - averagingInterval));
                    endOfInvalidation = new MillisecondsTimePoint(markPassing.getTimePoint().asMillis()
                            + averagingInterval);
                } else {
                    TimePoint[] interval = new TimePoint[] { oldMarkPassing.getTimePoint(), markPassing.getTimePoint() };
                    Arrays.sort(interval);
                    startOfInvalidation = getDummyFixWithConfidence(new MillisecondsTimePoint(interval[0].asMillis()
                            - averagingInterval));
                    endOfInvalidation = new MillisecondsTimePoint(interval[1].asMillis() + averagingInterval);
                }
                scheduleCacheRefresh(startOfInvalidation, endOfInvalidation);
            }
        }
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        clearCache();
    }

    @Override
    public void markPositionChanged(GPSFix fix, Mark mark) {
        assert fix != null && fix.getTimePoint() != null;
        // A mark position change can mean a leg type change. The interval over which the wind estimation is affected
        // depends on how the GPS track computes the estimated mark position. Ask it:
        Pair<TimePoint, TimePoint> interval = getTrackedRace().getOrCreateTrack(mark).getEstimatedPositionTimePeriodAffectedBy(fix);
        WindWithConfidence<TimePoint> startOfInvalidation = getDummyFixWithConfidence(interval.getA());
        TimePoint endOfInvalidation = interval.getB();
        scheduleCacheRefresh(startOfInvalidation, endOfInvalidation);
    }

    /**
     * As opposed to the superclass implementation, this variant checks if the {@link EstimatedWindFixesAsNavigableSet#floor(Wind)}
     * or {@link EstimatedWindFixesAsNavigableSet#ceiling(Wind)} is closer to <code>at</code> and returns the wind fix with confidence
     * from {@link #virtualInternalRawFixes} for the resolution-compliant time point closer to <code>at</code>.<p>
     * 
     * The current implementation doesn't consider the position <code>p</code> for the result's confidence, even though the
     * result type suggests it.
     */
    @Override
    protected WindWithConfidence<Pair<Position, TimePoint>> getAveragedWindUnsynchronized(Position p, TimePoint at) {
        lockForRead();
        try {
            TimePoint floorTimePoint = virtualInternalRawFixes.floorToResolution(at);
            TimePoint timePoint;
            if (floorTimePoint.equals(at)
                    || Math.abs(floorTimePoint.asMillis() - at.asMillis()) < Math.abs(virtualInternalRawFixes
                            .ceilingToResolution(at).asMillis() - at.asMillis())) {
                timePoint = floorTimePoint;
            } else {
                timePoint = virtualInternalRawFixes.ceilingToResolution(at);
            }
            WindWithConfidence<TimePoint> preResult = virtualInternalRawFixes.getWindWithConfidence(p, timePoint);
            // reduce confidence depending on how far *at* is away from the time point of the fix obtained
            double confidenceMultiplier = weigher.getConfidence(timePoint, at);
            WindWithConfidenceImpl<Pair<Position, TimePoint>> result = preResult == null ? null
                    : new WindWithConfidenceImpl<Pair<Position, TimePoint>>(preResult.getObject(), confidenceMultiplier
                            * preResult.getConfidence(),
                    /* relativeTo */new Pair<Position, TimePoint>(p, at), preResult.useSpeed());
            return result;
        } finally {
            unlockAfterRead();
        }
    }
    
    @Override
    public String toString() {
        lockForRead();
        try {
            return "This is the " + this.getClass().getName() + " object from " + virtualInternalRawFixes.getFrom()
                    + " to " + virtualInternalRawFixes.getTo() + " for race " + getTrackedRace();
        } finally {
            unlockAfterRead();
        }
    }
    
    /**
     * Emulates a collection of {@link Wind} fixes for a {@link TrackedRace}, computed using
     * {@link TrackedRace#getEstimatedWindDirection(com.sap.sailing.domain.base.Position, TimePoint)}. If not constrained
     * by a {@link #from} and/or a {@link #to} time point, an equidistant time field is assumed, starting at
     * {@link TrackedRace#getStart()} and leading up to {@link TrackedRace#getTimePointOfNewestEvent()}. If
     * {@link TrackedRace#getStart()} returns <code>null</code>, {@link Long#MAX_VALUE} is used as the {@link #from}
     * time point, pushing the start to the more or less infinite future ("end of the universe"). If no event was
     * received yet and hence {@link TrackedRace#getTimePointOfNewestEvent()} returns <code>null</code>, the {@link #to}
     * end is assumed to be the beginning of the epoch (1970-01-01T00:00:00).
     * 
     * @author Axel Uhl (d043530)
     * 
     */
    public class EstimatedWindFixesAsNavigableSet extends VirtualWindFixesAsNavigableSet {
        private static final long serialVersionUID = -6902341522276949873L;

        public EstimatedWindFixesAsNavigableSet(TrackedRace trackedRace) {
            this(trackedRace, null, null);
        }
        
        protected TrackBasedEstimationWindTrackImpl getTrack() {
            return (TrackBasedEstimationWindTrackImpl) super.getTrack();
        }
        
        /**
         * @param from expected to be an integer multiple of {@link #getResolutionInMilliseconds()} or <code>null</code>
         * @param to expected to be an integer multiple of {@link #getResolutionInMilliseconds()} or <code>null</code>
         */
        private EstimatedWindFixesAsNavigableSet(TrackedRace trackedRace,
                TimePoint from, TimePoint to) {
            super(TrackBasedEstimationWindTrackImpl.this, trackedRace, from, to, /* resolution in milliseconds */ 1000l);
        }

        protected Wind getWind(Position p, TimePoint timePoint) {
            final WindWithConfidence<TimePoint> estimatedWindDirectionWithConfidence = getWindWithConfidence(p, timePoint);
            return estimatedWindDirectionWithConfidence == null ? null : estimatedWindDirectionWithConfidence.getObject();
        }
        
        protected WindWithConfidence<TimePoint> getWindWithConfidence(Position p, TimePoint timePoint) {
            return getTrack().getEstimatedWindDirection(p, timePoint);
        }

        @Override
        protected NavigableSet<Wind> createSubset(WindTrack track, TrackedRace trackedRace, TimePoint from, TimePoint to) {
            return new EstimatedWindFixesAsNavigableSet(trackedRace, from, to);
        }

    }
}
