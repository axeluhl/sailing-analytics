package com.sap.sailing.domain.tracking.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.AbstractTimePoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.util.impl.ArrayListNavigableSet;

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
 * cache. If the {@link #speedAveragingChanged(long, long) speed averaging changes}, the entire cache is cleared.
 * 
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class TrackBasedEstimationWindTrackImpl extends WindTrackImpl implements RaceChangeListener<Competitor> {
    private final EstimatedWindFixesAsNavigableSet virtualInternalRawFixes;

    private final TrackedRace trackedRace;
    
    private final NavigableSet<TimePoint> timePointsWithCachedNullResult;

    public TrackBasedEstimationWindTrackImpl(TrackedRace trackedRace, long millisecondsOverWhichToAverage) {
        super(millisecondsOverWhichToAverage);
        this.trackedRace = trackedRace;
        trackedRace.addListener(this);
        this.virtualInternalRawFixes = new EstimatedWindFixesAsNavigableSet(this, trackedRace);
        this.timePointsWithCachedNullResult = new ArrayListNavigableSet<TimePoint>(AbstractTimePoint.TIMEPOINT_COMPARATOR);
    }

    private NavigableSet<Wind> getCachedFixes() {
        return super.getInternalRawFixes();
    }
    
    private NavigableSet<TimePoint> getTimePointsWithCachedNullResult() {
        return timePointsWithCachedNullResult;
    }

    protected synchronized void cache(TimePoint timePoint, Wind fix) {
        if (fix == null) {
            getTimePointsWithCachedNullResult().add(timePoint);
        } else {
            getCachedFixes().add(fix);
        }
    }
    
    protected synchronized void cacheNull(TimePoint timePoint) {
        timePointsWithCachedNullResult.add(timePoint);
    }
    
    private synchronized void invalidateCache(Wind startOfInvalidation, TimePoint endOfInvalidation) {
        NavigableSet<Wind> cache = getCachedFixes();
        Iterator<Wind> iter = cache.tailSet(startOfInvalidation, /* inclusive */ true).iterator();
        while (iter.hasNext()) {
            Wind next = iter.next();
            if (next.getTimePoint().compareTo(endOfInvalidation) < 0) {
                iter.remove();
            } else {
                break;
            }
        }
        Iterator<TimePoint> nullIter = getTimePointsWithCachedNullResult().tailSet(startOfInvalidation.getTimePoint(), /* inclusive */
                true).iterator();
        while (nullIter.hasNext()) {
            TimePoint next = nullIter.next();
            if (next.compareTo(endOfInvalidation) < 0) {
                nullIter.remove();
            } else {
                break;
            }
        }
    }

    private synchronized void clearCache() {
        getCachedFixes().clear();
        getTimePointsWithCachedNullResult().clear();
    }

    /**
     * Looks up wind data in the {@link #getCachedFixes() cache} and the {@link #getTimePointsWithCachedNullResult()
     * null store} first. Only if nothing is found for the time point requested, the
     * {@link TrackedRace#getEstimatedWindDirection(Position, TimePoint) wind estimation algorithm} is used to compute
     * it. The result will then be added to the cache.
     */
    protected synchronized Wind getEstimatedWindDirection(Position p, TimePoint timePoint) {
        Wind result;
        if (getTimePointsWithCachedNullResult().contains(timePoint)) {
            result = null;
        } else {
            NavigableSet<Wind> cache = getCachedFixes();
            Wind cachedFix = cache.floor(getDummyFix(timePoint));
            if (cachedFix == null || !cachedFix.getTimePoint().equals(timePoint)) {
                result = trackedRace.getEstimatedWindDirection(p, timePoint);
                cache(timePoint, result);
            } else {
                result = cachedFix;
            }
        }
        return result;
    }

    @Override
    protected NavigableSet<Wind> getInternalRawFixes() {
        return virtualInternalRawFixes;
    }

    @Override
    protected NavigableSet<Wind> getInternalFixes() {
        return new PartialNavigableSetView<Wind>(getInternalRawFixes()) {
            @Override
            protected boolean isValid(Wind e) {
                return e != null;
            }
        };
    }

    /**
     * This redefinition avoids very long searches in case <code>at</code> is before the race start or after the race's
     * newest event. Should <code>at</code> be out of this range, it is set to the closest border of this range before
     * calling the base class's implementation. If either race start or time of newest event are not known, the known
     * time point is used instead. If both time points are not known, <code>null</code> is returned immediately.
     */
    @Override
    public Wind getEstimatedWind(Position p, TimePoint at) {
        Wind result = null;
        TimePoint adjustedAt;
        TimePoint raceStartTimePoint = trackedRace.getStart();
        TimePoint timePointOfNewestEvent = trackedRace.getTimePointOfNewestEvent();
        if (raceStartTimePoint != null) {
            if (timePointOfNewestEvent != null) {
                if (at.compareTo(raceStartTimePoint) < 0) {
                    adjustedAt = raceStartTimePoint;
                } else if (at.compareTo(timePointOfNewestEvent) > 0) {
                    adjustedAt = timePointOfNewestEvent;
                } else {
                    adjustedAt = at;
                }
            } else {
                adjustedAt = raceStartTimePoint;
            }
        } else {
            if (timePointOfNewestEvent != null) {
                adjustedAt = timePointOfNewestEvent;
            } else {
                adjustedAt = null;
            }
        }
        if (adjustedAt != null) {
            // we can use the unsynchronized version here because our getInternalFixes() method operates
            // only on a virtual sequence of wind fixes where no concurrency issues have to be observed
            result = getEstimatedWindUnsynchronized(p, adjustedAt);
        }
        return result;
    }

    @Override
    public void windDataReceived(Wind wind) {
        // we ignore wind updates for cache invalidations; however, TODO we'd like to invalidate in case a leg changes its type
    }

    @Override
    public void windDataRemoved(Wind wind) {
        // we ignore wind updates for cache invalidations; however, TODO we'd like to invalidate in case a leg changes its type
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        // we ignore wind updates for cache invalidations; however, TODO we'd like to invalidate in case a leg changes its type
    }

    @Override
    public void gpsFixReceived(GPSFix fix, Competitor competitor) {
        long averagingInterval = trackedRace.getMillisecondsOverWhichToAverageSpeed();
        Wind startOfInvalidation = getDummyFix(new MillisecondsTimePoint(fix.getTimePoint().asMillis()-averagingInterval));
        TimePoint endOfInvalidation = new MillisecondsTimePoint(fix.getTimePoint().asMillis()+averagingInterval);
        invalidateCache(startOfInvalidation, endOfInvalidation);
    }

    @Override
    public void markPassingReceived(MarkPassing oldMarkPassing, MarkPassing markPassing) {
        long averagingInterval = trackedRace.getMillisecondsOverWhichToAverageSpeed();
        Wind startOfInvalidation;
        TimePoint endOfInvalidation;
        if (oldMarkPassing == null) {
            startOfInvalidation = getDummyFix(new MillisecondsTimePoint(markPassing.getTimePoint().asMillis()-averagingInterval));
            endOfInvalidation = new MillisecondsTimePoint(markPassing.getTimePoint().asMillis()+averagingInterval);
        } else {
            TimePoint[] interval = new TimePoint[] { oldMarkPassing.getTimePoint(), markPassing.getTimePoint() };
            Arrays.sort(interval);
            startOfInvalidation = getDummyFix(new MillisecondsTimePoint(interval[0].asMillis()-averagingInterval));
            endOfInvalidation = new MillisecondsTimePoint(interval[1].asMillis()+averagingInterval);
        }
        invalidateCache(startOfInvalidation, endOfInvalidation);
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        clearCache();
    }

}
