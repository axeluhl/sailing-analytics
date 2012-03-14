package com.sap.sailing.domain.tracking.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.AbstractTimePoint;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.confidence.ConfidenceFactory;
import com.sap.sailing.domain.confidence.Weigher;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
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
 * cache. If the {@link #speedAveragingChanged(long, long) speed averaging changes}, the entire cache is cleared.<p>
 * 
 * Note the {@link #getMillisecondsOverWhichToAverageWind() reduced averaging interval} used by this track type.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class TrackBasedEstimationWindTrackImpl extends VirtualWindTrackImpl implements RaceChangeListener {
    private static final SpeedWithBearing defaultSpeedWithBearing = new KnotSpeedWithBearingImpl(0, new DegreeBearingImpl(0));

    private final EstimatedWindFixesAsNavigableSet virtualInternalRawFixes;

    private final NavigableSet<TimePoint> timePointsWithCachedNullResult;
    
    private final NavigableSet<WindWithConfidence<TimePoint>> cache;
    
    private final Weigher<TimePoint> weigher;
    
    /**
     * A copy of the {@link #timePointsWithCachedNullResult} contents offering fast contains checks.
     */
    private final HashSet<TimePoint> timePointsWithCachedNullResultFastContains;

    public TrackBasedEstimationWindTrackImpl(TrackedRace trackedRace, long millisecondsOverWhichToAverage, double baseConfidence) {
        super(trackedRace, millisecondsOverWhichToAverage, baseConfidence,
                WindSourceType.TRACK_BASED_ESTIMATION.useSpeed());
        cache = new ArrayListNavigableSet<WindWithConfidence<TimePoint>>(new Comparator<WindWithConfidence<TimePoint>>() {
            @Override
            public int compare(WindWithConfidence<TimePoint> o1, WindWithConfidence<TimePoint> o2) {
                return o1.getObject().getTimePoint().compareTo(o2.getObject().getTimePoint());
            }
        });
        virtualInternalRawFixes = new EstimatedWindFixesAsNavigableSet(trackedRace);
        weigher = ConfidenceFactory.INSTANCE.createHyperbolicTimeDifferenceWeigher(getMillisecondsOverWhichToAverageWind());
        trackedRace.addListener(this);
        this.timePointsWithCachedNullResult = new ArrayListNavigableSet<TimePoint>(AbstractTimePoint.TIMEPOINT_COMPARATOR);
        this.timePointsWithCachedNullResultFastContains = new HashSet<TimePoint>();
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
    
    private NavigableSet<TimePoint> getTimePointsWithCachedNullResult() {
        return timePointsWithCachedNullResult;
    }

    protected synchronized void cache(TimePoint timePoint, WindWithConfidence<TimePoint> fix) {
        if (fix == null) {
            getTimePointsWithCachedNullResult().add(timePoint);
            timePointsWithCachedNullResultFastContains.add(timePoint);
        } else {
            getCachedFixes().add(fix);
        }
    }
    
    protected synchronized void cacheNull(TimePoint timePoint) {
        timePointsWithCachedNullResult.add(timePoint);
        timePointsWithCachedNullResultFastContains.add(timePoint);
    }
    
    private synchronized void invalidateCache(WindWithConfidence<TimePoint> startOfInvalidation, TimePoint endOfInvalidation) {
        Iterator<WindWithConfidence<TimePoint>> iter = (startOfInvalidation == null ? getCachedFixes()
                : getCachedFixes().tailSet(startOfInvalidation, /* inclusive */true)).iterator();
        while (iter.hasNext()) {
            WindWithConfidence<TimePoint> next = iter.next();
            if (endOfInvalidation == null || next.getObject().getTimePoint().compareTo(endOfInvalidation) < 0) {
                iter.remove();
            } else {
                break;
            }
        }
        Iterator<TimePoint> nullIter = (startOfInvalidation == null ? getTimePointsWithCachedNullResult()
                : getTimePointsWithCachedNullResult().tailSet(startOfInvalidation.getObject().getTimePoint(), /* inclusive */
                true)).iterator();
        while (nullIter.hasNext()) {
            TimePoint next = nullIter.next();
            if (endOfInvalidation == null || next.compareTo(endOfInvalidation) < 0) {
                nullIter.remove();
                timePointsWithCachedNullResultFastContains.remove(next);
            } else {
                break;
            }
        }
    }

    private synchronized void clearCache() {
        getCachedFixes().clear();
        timePointsWithCachedNullResult.clear();
        timePointsWithCachedNullResultFastContains.clear();
    }

    /**
     * Looks up wind data in the {@link #getCachedFixes() cache} and the {@link #getTimePointsWithCachedNullResult()
     * null store} first. Only if nothing is found for the time point requested, the
     * {@link TrackedRace#getEstimatedWindDirection(Position, TimePoint) wind estimation algorithm} is used to compute
     * it. The result will then be added to the cache.
     */
    private synchronized WindWithConfidence<TimePoint> getEstimatedWindDirection(Position p, TimePoint timePoint) {
        WindWithConfidence<TimePoint> result;
        if (nullResultCacheContains(timePoint)) {
            result = null;
        } else {
            WindWithConfidence<TimePoint> cachedFix = getCachedFixes().floor(getDummyFixWithConfidence(timePoint));
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
        return timePointsWithCachedNullResultFastContains.contains(timePoint);
    }

    @Override
    public void windDataReceived(Wind wind) {
        invalidateForNewWind(wind);
    }

    private void invalidateForNewWind(Wind wind) {
        long averagingInterval = getTrackedRace().getMillisecondsOverWhichToAverageWind();
        WindWithConfidence<TimePoint> startOfInvalidation = getDummyFixWithConfidence(new MillisecondsTimePoint(wind
                .getTimePoint().asMillis() - averagingInterval));
        TimePoint endOfInvalidation = new MillisecondsTimePoint(wind.getTimePoint().asMillis() + averagingInterval);
        invalidateCache(startOfInvalidation, endOfInvalidation);
    }

    @Override
    public void windDataRemoved(Wind wind) {
        invalidateForNewWind(wind);
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        clearCache();
    }

    @Override
    public void competitorPositionChanged(GPSFix fix, Competitor competitor) {
        long averagingInterval = getTrackedRace().getMillisecondsOverWhichToAverageSpeed();
        WindWithConfidence<TimePoint> startOfInvalidation = getDummyFixWithConfidence(new MillisecondsTimePoint(fix
                .getTimePoint().asMillis() - averagingInterval));
        TimePoint endOfInvalidation = new MillisecondsTimePoint(fix.getTimePoint().asMillis() + averagingInterval);
        invalidateCache(startOfInvalidation, endOfInvalidation);
    }

    @Override
    public void markPassingReceived(MarkPassing oldMarkPassing, MarkPassing markPassing) {
        long averagingInterval = getTrackedRace().getMillisecondsOverWhichToAverageSpeed();
        WindWithConfidence<TimePoint> startOfInvalidation;
        TimePoint endOfInvalidation;
        if (oldMarkPassing == null) {
            startOfInvalidation = getDummyFixWithConfidence(new MillisecondsTimePoint(markPassing.getTimePoint().asMillis()-averagingInterval));
            endOfInvalidation = new MillisecondsTimePoint(markPassing.getTimePoint().asMillis()+averagingInterval);
        } else {
            TimePoint[] interval = new TimePoint[] { oldMarkPassing.getTimePoint(), markPassing.getTimePoint() };
            Arrays.sort(interval);
            startOfInvalidation = getDummyFixWithConfidence(new MillisecondsTimePoint(interval[0].asMillis()-averagingInterval));
            endOfInvalidation = new MillisecondsTimePoint(interval[1].asMillis()+averagingInterval);
        }
        invalidateCache(startOfInvalidation, endOfInvalidation);
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        clearCache();
    }

    @Override
    public void buoyPositionChanged(GPSFix fix, Buoy buoy) {
        // A buoy position change can mean a leg type change. The interval over which the wind estimation is affected
        // depends on how the GPS track computes the estimated buoy position. Ask it:
        Pair<TimePoint, TimePoint> interval = getTrackedRace().getOrCreateTrack(buoy).getEstimatedPositionTimePeriodAffectedBy(fix);
        WindWithConfidence<TimePoint> startOfInvalidation = interval.getA() == null ? null : getDummyFixWithConfidence(interval.getA());
        TimePoint endOfInvalidation = interval.getB();
        invalidateCache(startOfInvalidation, endOfInvalidation);
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
        TimePoint floorTimePoint = virtualInternalRawFixes.floorToResolution(at);
        TimePoint timePoint;
        if (floorTimePoint.equals(at) ||
                Math.abs(floorTimePoint.asMillis() - at.asMillis()) <
                Math.abs(virtualInternalRawFixes.ceilingToResolution(at).asMillis() - at.asMillis())) {
            timePoint = floorTimePoint;
        } else {
            timePoint = virtualInternalRawFixes.ceilingToResolution(at);
        }
        WindWithConfidence<TimePoint> preResult = virtualInternalRawFixes.getWindWithConfidence(p, timePoint);
        // reduce confidence depending on how far *at* is away from the time point of the fix obtained
        double confidenceMultiplier = weigher.getConfidence(timePoint, at);
        WindWithConfidenceImpl<Pair<Position, TimePoint>> result = preResult == null ? null :
            new WindWithConfidenceImpl<Pair<Position, TimePoint>>(
                preResult.getObject(), confidenceMultiplier * preResult.getConfidence(),
                /* relativeTo */ new Pair<Position, TimePoint>(p, at), preResult.useSpeed());
        return result;
    }
    
    @Override
    public String toString() {
        return "This is the " + this.getClass().getName() + " object from " + virtualInternalRawFixes.getFrom()
                + " to " + virtualInternalRawFixes.getTo() + " for race " + getTrackedRace();
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
