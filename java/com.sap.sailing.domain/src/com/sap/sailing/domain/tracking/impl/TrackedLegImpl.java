package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.base.SpeedWithConfidence;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.TargetTimeInfoImpl;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.polars.NotEnoughDataHasBeenAddedException;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.MarkPositionAtTimePointCache;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRaceStatus;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackedLegImpl implements TrackedLeg {
    private static final long serialVersionUID = -1944668527284130545L;

    private final static Logger logger = Logger.getLogger(TrackedLegImpl.class.getName());
    
    private final Leg leg;
    private final Map<Competitor, TrackedLegOfCompetitor> trackedLegsOfCompetitors;
    private TrackedRaceImpl trackedRace;
    private transient ConcurrentMap<TimePoint, List<TrackedLegOfCompetitor>> competitorTracksOrderedByRank;
    
    public TrackedLegImpl(DynamicTrackedRaceImpl trackedRace, Leg leg, Iterable<Competitor> competitors) {
        super();
        this.leg = leg;
        this.trackedRace = trackedRace;
        trackedLegsOfCompetitors = new HashMap<Competitor, TrackedLegOfCompetitor>();
        for (Competitor competitor : competitors) {
            trackedLegsOfCompetitors.put(competitor, new TrackedLegOfCompetitorImpl(this, competitor, trackedRace.getBoatOfCompetitor(competitor)));
        }
        trackedRace.addListener(new CacheClearingRaceChangeListener());
        competitorTracksOrderedByRank = new ConcurrentHashMap<>();
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        competitorTracksOrderedByRank = new ConcurrentHashMap<>();
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        final Course course = trackedRace.getRace().getCourse();
        course.lockForRead();
        try {
            oos.defaultWriteObject();
        } finally {
            course.unlockAfterRead();
        }
    }
    
    @Override
    public Leg getLeg() {
        return leg;
    }
    
    @Override
    public TrackedRaceImpl getTrackedRace() {
        return trackedRace;
    }

    @Override
    public Iterable<TrackedLegOfCompetitor> getTrackedLegsOfCompetitors() {
        return trackedLegsOfCompetitors.values();
    }

    @Override
    public TrackedLegOfCompetitor getTrackedLeg(Competitor competitor) {
        return trackedLegsOfCompetitors.get(competitor);
    }

    @Override
    public Competitor getLeader(TimePoint timePoint) {
        List<TrackedLegOfCompetitor> byRank = getCompetitorTracksOrderedByRank(timePoint);
        return byRank.get(0).getCompetitor();
    }

    @Override
    public Competitor getLeader(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        List<TrackedLegOfCompetitor> byRank = getCompetitorTracksOrderedByRank(timePoint, cache);
        return byRank.get(0).getCompetitor();
    }

    /**
     * Orders the tracked legs for all competitors for this tracked leg for the given time point. This
     * results in an order that gives a ranking for this tracked leg. In particular, boats that have not
     * yet entered this leg will all be ranked equal because their windward distance to go is the full
     * leg's winward distance. Boats who already finished this leg have their tracks ordered by the time
     * points at which they finished the leg.<p>
     * 
     * Note that this does not reflect overall race standings. For that, the ordering would have to
     * consider the order of the boats not currently in this leg, too.
     */
    protected List<TrackedLegOfCompetitor> getCompetitorTracksOrderedByRank(TimePoint timePoint) {
        return getCompetitorTracksOrderedByRank(timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
    }
    
    List<TrackedLegOfCompetitor> getCompetitorTracksOrderedByRank(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        List<TrackedLegOfCompetitor> rankedCompetitorList;
        rankedCompetitorList = competitorTracksOrderedByRank.get(timePoint);
        if (rankedCompetitorList != null) {
            rankedCompetitorList = new ArrayList<TrackedLegOfCompetitor>(rankedCompetitorList);
        }
        if (rankedCompetitorList == null) {
            rankedCompetitorList = new ArrayList<TrackedLegOfCompetitor>();
            for (TrackedLegOfCompetitor competitorLeg : getTrackedLegsOfCompetitors()) {
                rankedCompetitorList.add(competitorLeg);
            }
            // race may be updated while calculation is going on, but each individual calculation is properly
            // synchronized, usually by read-write locks, so there is no major difference in synchronization issues
            // an the asynchronous nature of how the data is being received
            Collections.sort(rankedCompetitorList, getTrackedRace().getRankingMetric().getLegRankingComparator(this, timePoint, cache));
            rankedCompetitorList = Collections.unmodifiableList(rankedCompetitorList);
            competitorTracksOrderedByRank.put(timePoint, rankedCompetitorList);
            if (Util.size(getTrackedLegsOfCompetitors()) != rankedCompetitorList.size()) {
                logger.warning("Number of competitors in leg (" + Util.size(getTrackedLegsOfCompetitors())
                        + ") differs from number of competitors in race ("
                        + Util.size(getTrackedRace().getRace().getCompetitors()) + ")");
            }
        }
        return rankedCompetitorList;
    }
    
    @Override
    public LinkedHashMap<Competitor, Integer> getRanks(TimePoint timePoint) {
        return getRanks(timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
    }
    
    @Override
    public LinkedHashMap<Competitor, Integer> getRanks(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        List<TrackedLegOfCompetitor> orderedTrackedLegsOfCompetitors = getCompetitorTracksOrderedByRank(timePoint, cache);
        LinkedHashMap<Competitor, Integer> result = new LinkedHashMap<Competitor, Integer>();
        int i=1;
        for (TrackedLegOfCompetitor tloc : orderedTrackedLegsOfCompetitors) {
            result.put(tloc.getCompetitor(), i++);
        }
        return result;
    }
    
    @Override
    public LegType getLegType(TimePoint at) throws NoWindException {
        Wind wind = getWindOnLeg(at);
        if (wind == null) {
            throw new NoWindException("Need to know wind direction in race "+getTrackedRace().getRace().getName()+
                    " to determine whether leg "+getLeg()+
                    " is an upwind or downwind leg");
        }
        Bearing legBearing = getLegBearing(at);
        if (legBearing != null) {
            double deltaDeg = legBearing.getDifferenceTo(wind.getBearing()).getDegrees();
            if (Math.abs(deltaDeg) < LegType.UPWIND_DOWNWIND_TOLERANCE_IN_DEG) {
                return LegType.DOWNWIND;
            } else {
                double deltaDegOpposite = legBearing.getDifferenceTo(wind.getBearing().reverse()).getDegrees();
                if (Math.abs(deltaDegOpposite) < LegType.UPWIND_DOWNWIND_TOLERANCE_IN_DEG) {
                    return LegType.UPWIND;
                }
            }
        }
        return LegType.REACHING;
    }

    @Override
    public Bearing getLegBearing(TimePoint at) {
        return getLegBearing(at, new MarkPositionAtTimePointCacheImpl(getTrackedRace(), at));
    }

    @Override
    public Bearing getLegBearing(TimePoint at, MarkPositionAtTimePointCache markPositionCache) {
        assert markPositionCache.getTimePoint().equals(at);
        assert markPositionCache.getTrackedRace() == getTrackedRace();
        Position startMarkPos = markPositionCache.getApproximatePosition(getLeg().getFrom());
        Position endMarkPos = markPositionCache.getApproximatePosition(getLeg().getTo());
        Bearing legBearing = (startMarkPos != null && endMarkPos != null) ? startMarkPos.getBearingGreatCircle(endMarkPos) : null;
        return legBearing;
    }

    @Override
    public boolean isUpOrDownwindLeg(TimePoint at) throws NoWindException {
        return getLegType(at) != LegType.REACHING;
    }

    private Wind getWindOnLeg(TimePoint at) {
        final Wind wind;
        final Position middleOfLeg = getMiddleOfLeg(at);
        if (middleOfLeg == null) {
            wind = null;
        } else {
            Set<WindSource> windSourcesToExclude = new HashSet<>(getTrackedRace().getWindSourcesToExclude());
            windSourcesToExclude.addAll(getTrackedRace().getWindSources(WindSourceType.TRACK_BASED_ESTIMATION));
            wind = getWind(middleOfLeg, at, windSourcesToExclude);
        }
        return wind;
    }

    /**
     * @return the approximate position in the middle of the leg. The position is determined by using the position
     * of the first mark at the beginning of the leg and moving half way to the first mark of leg's end. If either of
     * the mark positions cannot be determined, <code>null</code> is returned.
     */
    @Override
    public Position getMiddleOfLeg(TimePoint at) {
        Position approximateLegStartPosition = getTrackedRace().getApproximatePosition(getLeg().getFrom(), at);
        Position approximateLegEndPosition = getTrackedRace().getApproximatePosition(getLeg().getTo(), at);
        final Position middleOfLeg;
        if (approximateLegStartPosition == null || approximateLegEndPosition == null) {
            middleOfLeg = null;
        } else {
            // exclude track-based estimation; it is itself based on the leg type which is based on the getWindOnLeg
            // result which
            // would therefore lead to an endless recursion without further tricks being applied
            middleOfLeg = approximateLegStartPosition.translateGreatCircle(
                    approximateLegStartPosition.getBearingGreatCircle(approximateLegEndPosition),
                    approximateLegStartPosition.getDistance(approximateLegEndPosition).scale(0.5));
        }
        return middleOfLeg;
    }
    
    public Position getEffectiveWindPosition(Callable<Position> exactPositionProvider, TimePoint at, WindPositionMode mode) {
        final Position effectivePosition;
        switch (mode) {
        case EXACT:
            try {
                effectivePosition = exactPositionProvider.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            break;
        case LEG_MIDDLE:
            effectivePosition = getMiddleOfLeg(at);
            break;
        case GLOBAL_AVERAGE:
            effectivePosition = null;
            break;
        default:
            effectivePosition = null;
            logger.info("Strange: don't know WindPositionMode literal "+mode.name());
        }
        return effectivePosition;
    }

    private Wind getWind(Position p, TimePoint at, Set<WindSource> windSourcesToExclude) {
        return getTrackedRace().getWind(p, at, windSourcesToExclude);
    }

    /**
     * The default action of this listener is to {@link #clearCaches clear the tracked leg's caches} when anything on
     * the race changes. There are a few exceptions that don't necessitate a cache clearing, so these methods are
     * overridden here to not call the {@link #defaultAction} which here delegates to {@link #clearCaches}. For
     * {@link #waypointAdded} clearing the caches is necessary because the competitor tracks ordered by rank may change
     * for legs adjacent to the waypoint added } // and because the leg's from/to waypoints may have changed. For
     * {@link #waypointRemoved} it is necessary because the competitor tracks ordered by rank may change for legs
     * adjacent to the waypoint removed.
     * 
     * @author Axel Uhl (D043530)
     *
     */
    private class CacheClearingRaceChangeListener extends AbstractRaceChangeListener implements Serializable {
        private static final long serialVersionUID = 4455608396760152359L;

        @Override
        protected void defaultAction() {
            clearCaches();
        }

        /**
         * no-op; the leg doesn't mind the tracked race's status being updated
         */
        @Override
        public void statusChanged(TrackedRaceStatus newStatus, TrackedRaceStatus oldStatus) {
        }

        /**
         * no-op; no change of competitorTracksOrderedByRank necessary
         */
        @Override
        public void delayToLiveChanged(long delayToLiveInMillis) {
        }
    }

    private void clearCaches() {
        competitorTracksOrderedByRank.clear();
    }

    @Override
    public void waypointsMayHaveChanges() {
        clearCaches();
    }

    @Override
    public Distance getAbsoluteCrossTrackError(Position p, TimePoint timePoint) {
        final Position approximatePosition = getTrackedRace().getApproximatePosition(getLeg().getFrom(), timePoint);
        final Bearing legBearing = getLegBearing(timePoint);
        return approximatePosition==null || legBearing==null ? null : p.absoluteCrossTrackError(approximatePosition, legBearing);
    }

    @Override
    public Distance getSignedCrossTrackError(Position p, TimePoint timePoint) {
        final Position approximatePosition = getTrackedRace().getApproximatePosition(getLeg().getFrom(), timePoint);
        final Bearing legBearing = getLegBearing(timePoint);
        return approximatePosition==null || legBearing==null ? null : p.crossTrackError(approximatePosition, legBearing);
    }

    @Override
    public Distance getGreatCircleDistance(TimePoint timePoint, MarkPositionAtTimePointCache markPositionCache) {
        assert markPositionCache.getTimePoint().equals(timePoint);
        assert markPositionCache.getTrackedRace() == getTrackedRace();
        final Distance result;
        final Position approximatePositionOfFrom = markPositionCache.getApproximatePosition(getLeg().getFrom());
        final Position approximatePositionOfTo = markPositionCache.getApproximatePosition(getLeg().getTo());
        if (approximatePositionOfFrom != null && approximatePositionOfTo != null) {
            result = approximatePositionOfFrom.getDistance(approximatePositionOfTo);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Distance getGreatCircleDistance(TimePoint timePoint) {
        return getGreatCircleDistance(timePoint, new NonCachingMarkPositionAtTimePointCache(getTrackedRace(), timePoint));
    }

    @Override
    public Distance getAbsoluteWindwardDistance(Position pos1, Position pos2, TimePoint at, WindPositionMode windPositionMode) {
        return getAbsoluteWindwardDistance(pos1, pos2, at, windPositionMode, new LeaderboardDTOCalculationReuseCache(at));
    }
    
    @Override
    public Distance getAbsoluteWindwardDistance(Position pos1, Position pos2, TimePoint at,
            WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        final Distance preResult = getWindwardDistance(pos1, pos2, at, windPositionMode, cache);
        final Distance result;
        if (preResult == null || preResult.getMeters() >= 0) {
            result = preResult;
        } else {
            result = new MeterDistance(-preResult.getMeters());
        }
        return result;
    }
    
    @Override
    public Distance getAbsoluteWindwardDistanceFromLegStart(Position pos) {
        final TimePoint referenceTimePoint = getReferenceTimePoint();
        return getAbsoluteWindwardDistanceFromLegStart(pos, referenceTimePoint, new LeaderboardDTOCalculationReuseCache(referenceTimePoint));
    }
    
    @Override
    public Distance getWindwardDistanceFromLegStart(Position pos, WindLegTypeAndLegBearingCache cache) {
        final TimePoint referenceTimePoint = getReferenceTimePoint();
        return getWindwardDistanceFromLegStart(pos, referenceTimePoint, cache);
    }

    private Distance getWindwardDistanceFromLegStart(Position pos, final TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return getWindwardDistance(getTrackedRace().getApproximatePosition(getLeg().getFrom(), timePoint),
                pos, timePoint, WindPositionMode.LEG_MIDDLE, cache);
    }

    @Override
    public Distance getAbsoluteWindwardDistanceFromLegStart(Position pos, WindLegTypeAndLegBearingCache cache) {
        final TimePoint referenceTimePoint = getReferenceTimePoint();
        return getAbsoluteWindwardDistanceFromLegStart(pos, referenceTimePoint, cache);
    }

    private Distance getAbsoluteWindwardDistanceFromLegStart(Position pos, final TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return getAbsoluteWindwardDistance(getTrackedRace().getApproximatePosition(getLeg().getFrom(), timePoint),
                pos, timePoint, WindPositionMode.LEG_MIDDLE, cache);
    }
    
    @Override
    public Distance getWindwardDistance(Position pos1, Position pos2, TimePoint at, WindPositionMode windPositionMode) {
        return getWindwardDistance(pos1, pos2, at, windPositionMode, new LeaderboardDTOCalculationReuseCache(at));
    }
    
    @Override
    public Distance getWindwardDistance() {
        return getWindwardDistance(getReferenceTimePoint());
    }
    
    private Distance getWindwardDistance(TimePoint timePoint) {
        return getWindwardDistance(timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
    }
    
    @Override
    public Distance getWindwardDistance(WindLegTypeAndLegBearingCache cache) {
        final TimePoint middle = getReferenceTimePoint();
        return getWindwardDistance(middle, cache);
    }

    @Override
    public Distance getAbsoluteWindwardDistance(WindLegTypeAndLegBearingCache cache) {
        final TimePoint middle = getReferenceTimePoint();
        return getAbsoluteWindwardDistance(middle, cache);
    }

    @Override
    public TimePoint getReferenceTimePoint() {
        Iterable<MarkPassing> legStartMarkPassings = getTrackedRace().getMarkPassingsInOrder(getLeg().getFrom());
        Iterable<MarkPassing> legFinishMarkPassings = getTrackedRace().getMarkPassingsInOrder(getLeg().getTo());
        getTrackedRace().lockForRead(legStartMarkPassings);
        final TimePoint firstLegStartMarkPassingTimePoint;
        final TimePoint lastLegFinishMarkPassingTimePoint;
        try {
            Iterator<MarkPassing> i = legStartMarkPassings.iterator();
            if (i.hasNext()) {
                firstLegStartMarkPassingTimePoint = i.next().getTimePoint();
            } else {
                firstLegStartMarkPassingTimePoint = MillisecondsTimePoint.now();
            }
        } finally {
            getTrackedRace().unlockAfterRead(legStartMarkPassings);
        }
        getTrackedRace().lockForRead(legFinishMarkPassings);
        try {
            Iterator<MarkPassing> i = legFinishMarkPassings.iterator();
            if (i.hasNext()) {
                lastLegFinishMarkPassingTimePoint = i.next().getTimePoint();
            } else {
                lastLegFinishMarkPassingTimePoint = MillisecondsTimePoint.now();
            }
        } finally {
            getTrackedRace().unlockAfterRead(legFinishMarkPassings);
        }
        final TimePoint middle = firstLegStartMarkPassingTimePoint.plus(firstLegStartMarkPassingTimePoint.until(lastLegFinishMarkPassingTimePoint).divide(2));
        return middle;
    }

    @Override
    public Distance getWindwardDistance(final TimePoint middle, WindLegTypeAndLegBearingCache cache) {
        final Position fromPos = getTrackedRace().getApproximatePosition(getLeg().getFrom(), middle);
        final Position toPos = getTrackedRace().getApproximatePosition(getLeg().getTo(), middle);
        return getWindwardDistance(fromPos, toPos, middle, WindPositionMode.LEG_MIDDLE, cache);
    }

    @Override
    public Distance getAbsoluteWindwardDistance(final TimePoint middle, WindLegTypeAndLegBearingCache cache) {
        final Position fromPos = getTrackedRace().getApproximatePosition(getLeg().getFrom(), middle);
        final Position toPos = getTrackedRace().getApproximatePosition(getLeg().getTo(), middle);
        return getAbsoluteWindwardDistance(fromPos, toPos, middle, WindPositionMode.LEG_MIDDLE, cache);
    }

    Distance getWindwardDistance(final Position pos1, final Position pos2, TimePoint at, WindPositionMode windPositionMode, WindLegTypeAndLegBearingCache cache) {
        Distance result;
        if (pos1 == null || pos2 == null) {
            result = null;
        } else {
            try {
                final LegType legType = cache.getLegType(this, at);
                if (legType != LegType.REACHING) { // upwind or downwind
                    final Position effectivePosition = getEffectiveWindPosition(() -> pos1.translateGreatCircle(
                            pos1.getBearingGreatCircle(pos2), pos1.getDistance(pos2).scale(0.5)), at, windPositionMode);
                    Wind wind = getTrackedRace().getWind(effectivePosition, at);
                    if (wind == null) {
                        result = pos2.alongTrackDistance(pos1, cache.getLegBearing(this, at));
                    } else {
                        Position projectionToLineThroughPos2 = pos1.projectToLineThrough(pos2, wind.getBearing());
                        result = pos2.alongTrackDistance(projectionToLineThroughPos2,
                                legType == LegType.UPWIND ? wind.getFrom() : wind.getBearing());
                    }
                } else {
                    // reaching leg, return distance projected onto leg's bearing
                    result = pos2.alongTrackDistance(pos1, cache.getLegBearing(this, at));
                }
            } catch (NoWindException e) {
                // no wind information; use along-track distance as fallback
                result = pos2.alongTrackDistance(pos1, cache.getLegBearing(this, at));
            }
        }
        return result;
    }

    @Override
    public TargetTimeInfo.LegTargetTimeInfo getEstimatedTimeAndDistanceToComplete(PolarDataService polarDataService, TimePoint timepoint, MarkPositionAtTimePointCache markPositionCache)
            throws NotEnoughDataHasBeenAddedException, NoWindException {
        assert timepoint.equals(markPositionCache.getTimePoint());
        assert getTrackedRace() == markPositionCache.getTrackedRace();
        Position centralPosition = getMiddleOfLeg(timepoint);
        Wind wind = trackedRace.getWind(centralPosition, timepoint);
        Position from = trackedRace.getApproximatePosition(leg.getFrom(), timepoint);
        Position to = trackedRace.getApproximatePosition(leg.getTo(), timepoint);
        LegType legType = getLegType(timepoint);
        BoatClass boatClass = trackedRace.getRace().getBoatClass();
        final Bearing legBearing = from.getBearingGreatCircle(to);
        Distance distance = from.getDistance(to);
        Bearing trueWindAngleToLeg = legBearing.getDifferenceTo(wind.getBearing().reverse());
        final Duration result;
        final Distance resultDistance;
        if (legType == LegType.REACHING) {
            SpeedWithConfidence<Void> reachSpeed = polarDataService.getSpeed(boatClass, wind, trueWindAngleToLeg);
            result = reachSpeed.getObject().getDuration(distance);
            resultDistance = distance;
        } else {
            SpeedWithBearingWithConfidence<Void> portSpeedAndBearing = polarDataService.getAverageSpeedWithBearing(
                    boatClass, wind, legType, Tack.PORT);
            SpeedWithBearingWithConfidence<Void> starboardSpeedAndBearing = polarDataService.getAverageSpeedWithBearing(
                    boatClass, wind, legType, Tack.STARBOARD);
            Pair<Distance, Duration> estimationPair = estimateTargetTimeTacking(from, to, portSpeedAndBearing,
                    starboardSpeedAndBearing, wind);
            result = estimationPair.getB();
            resultDistance = estimationPair.getA();
        }
        return new TargetTimeInfoImpl.LegTargetTimeInfoImpl(distance, wind, legBearing, result, timepoint, legType,
                resultDistance);
    }

    private Pair<Distance, Duration> estimateTargetTimeTacking(Position from, Position to,
            SpeedWithBearingWithConfidence<Void> portSpeedAndBearing,
            SpeedWithBearingWithConfidence<Void> starboardSpeedAndBearing, Wind wind) {
        Bearing portBearing = portSpeedAndBearing.getObject().getBearing();
        Bearing starboardBearing = starboardSpeedAndBearing.getObject().getBearing();
        Position intersection = from.getIntersection(portBearing, to, starboardBearing);
        Distance fromToIntersection = from.getDistance(intersection);
        Speed portSpeed = portSpeedAndBearing.getObject();
        Duration duration1 = portSpeed.getDuration(fromToIntersection);
        Distance fromToTo = intersection.getDistance(to);
        Speed starboardSpeed = starboardSpeedAndBearing.getObject();
        Duration duration2 = starboardSpeed.getDuration(fromToTo);
        return new Pair<Distance, Duration>(fromToIntersection.add(fromToTo), duration1.plus(duration2));
    }

    @Override
    public WindWithConfidence<Pair<Position, TimePoint>> getAverageTrueWindDirection() {
        final TimePoint timePoint = getReferenceTimePoint();
        return getTrackedRace().getWindWithConfidence(getMiddleOfLeg(timePoint), timePoint);
    }
    
    @Override
    public String toString() {
        return "TrackedLeg for "+getLeg();
    }
}
