package com.sap.sailing.domain.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.impl.AbstractRaceRankComparator;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public abstract class AbstractRankingMetric implements RankingMetric {
    private static final long serialVersionUID = -3671039530564696392L;
    private final TrackedRace trackedRace;
    
    public class CompetitorRankingInfoImpl implements RankingMetric.CompetitorRankingInfo {
        private static final long serialVersionUID = 2699792976562460961L;

        /**
         * For which time point in the race was this ranking information computed?
         */
        private final TimePoint timePoint;
        
        /**
         * Whose ranking does this object describe?
         */
        private final Competitor competitor;
        
        /**
         * How far did {@link #competitor} actually sail windward / along track from the start of the race until
         * {@link #timePoint}? <code>null</code> before the race start; {@link Distance#NULL} after the race start
         * until {@link #competitor} has actually started.
         */
        private final Distance windwardDistanceSailed;
        
        /**
         * Usually the difference between {@link #timePoint} and the start of the race
         */
        private final Duration actualTime;
        
        /**
         * The corrected time for the {@link #competitor}, assuming the race ended at {@link #timePoint}. This
         * is applying the handicaps proportionately to the time and distance the competitor sailed so far.
         */
        private final Duration correctedTime;
        
        /**
         * Based on the {@link #competitor}'s average VMG in the current leg and the windward position
         * of the competitor that is farthest ahead in the race, how long would it take {@link #competitor}
         * to reach the competitor farthest ahead if that competitor stopped at {@link #timePoint}?
         */
        private final Duration estimatedActualDurationToCompetitorFarthestAhead;
        
        /**
         * The corrections applied to the time and distance sailed when the {@link #competitor} would have reached the
         * competitor farthest ahead (which would be the case {@link #estimatedActualDurationToCompetitorFarthestAhead} after
         * {@link #timePoint}).
         */
        private final Duration correctedTimeAtEstimatedArrivalAtCompetitorFarthestAhead;

        protected CompetitorRankingInfoImpl(TimePoint timePoint, Competitor competitor, Distance windwardDistanceSailed,
                Duration actualTime, Duration correctedTime, Duration estimatedActualDurationToCompetitorFarthestAhead,
                Duration correctedTimeAtEstimatedArrivalAtCompetitorFarthestAhead) {
            super();
            this.timePoint = timePoint;
            this.competitor = competitor;
            this.windwardDistanceSailed = windwardDistanceSailed;
            this.actualTime = actualTime;
            this.correctedTime = correctedTime;
            this.estimatedActualDurationToCompetitorFarthestAhead = estimatedActualDurationToCompetitorFarthestAhead;
            this.correctedTimeAtEstimatedArrivalAtCompetitorFarthestAhead = correctedTimeAtEstimatedArrivalAtCompetitorFarthestAhead;
        }

        @Override
        public TimePoint getTimePoint() {
            return timePoint;
        }

        @Override
        public Competitor getCompetitor() {
            return competitor;
        }

        @Override
        public Distance getWindwardDistanceSailed() {
            return windwardDistanceSailed;
        }

        @Override
        public Duration getActualTime() {
            return actualTime;
        }

        @Override
        public Duration getCorrectedTime() {
            return correctedTime;
        }

        @Override
        public Duration getEstimatedActualDurationFromTimePointToCompetitorFarthestAhead() {
            return estimatedActualDurationToCompetitorFarthestAhead;
        }
        
        @Override
        public Duration getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead() {
            return correctedTimeAtEstimatedArrivalAtCompetitorFarthestAhead;
        }
    }

    /**
     * Helper instance used to encode <code>null</code> values in {@link ConcurrentHashMap} instances which do not accept
     * <code>null</code> as key nor value.
     */
    private final static Competitor NULL_COMPETITOR = new CompetitorImpl(null, null, null, null, null, null, null, null, null, null);
    
    public abstract class AbstractRankingInfo implements RankingMetric.RankingInfo {
        private static final long serialVersionUID = 6845168655725234325L;
        
        /**
         * The time point for which this ranking information is valid
         */
        private final TimePoint timePoint;
        
        /**
         * Caches, on demand, the results of calls to {@link #getCompetitorFarthestAheadInLeg(Leg, TimePoint, WindLegTypeAndLegBearingCache)}.
         * <code>null</code> values are encoded as the {@link #NULL_COMPETITOR} and must be translated back to <code>null</code> before returning
         * to a caller outside of this class.
         */
        private final ConcurrentMap<Leg, Competitor> competitorFarthestAheadInLeg;
        
        public AbstractRankingInfo(final TimePoint timePoint) {
            this.timePoint = timePoint;
            this.competitorFarthestAheadInLeg = new ConcurrentHashMap<>();
        }

        @Override
        public TimePoint getTimePoint() {
            return timePoint;
        }

        @Override
        public Duration getActualTimeFromRaceStartToReachFarthestAheadInLeg(Competitor competitor, Leg leg, WindLegTypeAndLegBearingCache cache) {
            final Duration result;
            final TrackedLegOfCompetitor tloc = getTrackedRace().getTrackedLeg(competitor, getTimePoint());
            final Duration raceDurationAtTimePoint = getTrackedRace().getStartOfRace().until(getTimePoint());
            if (tloc != null && tloc.hasStartedLeg(getTimePoint())) {
                final Competitor competitorFarthestAheadInLeg = getCompetitorFarthestAheadInLeg(leg, getTimePoint(), cache);
                final TrackedLegOfCompetitor tlocOfCompetitorFarthestAheadInLeg = tloc.getTrackedLeg().getTrackedLeg(competitorFarthestAheadInLeg);
                final Duration predictedDurationFromTimePointToReachFarthestAheadInLeg = getPredictedDurationToReachWindwardPositionOf(
                        tloc, tlocOfCompetitorFarthestAheadInLeg, getTimePoint(), cache);
                if (predictedDurationFromTimePointToReachFarthestAheadInLeg == null) {
                    result = null;
                } else {
                    final Duration cDurationFromRaceStartToReachFarthestInLeg = raceDurationAtTimePoint.plus(predictedDurationFromTimePointToReachFarthestAheadInLeg);
                    result = cDurationFromRaceStartToReachFarthestInLeg;
                }
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public Competitor getCompetitorFarthestAheadInLeg(Leg leg, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
            Competitor result = competitorFarthestAheadInLeg.get(leg);
            if (result == NULL_COMPETITOR) {
                result = null;
            } else if (result == null) {
                result = AbstractRankingMetric.this.getCompetitorFarthestAheadInLeg(getTrackedRace().getTrackedLeg(leg), timePoint, cache);
                competitorFarthestAheadInLeg.put(leg, result);
            }
            return result;
        }

        @Override
        public Competitor getLeaderInLegByCalculatedTime(Leg leg, WindLegTypeAndLegBearingCache cache) {
            final TrackedLeg trackedLeg = getTrackedRace().getTrackedLeg(leg);
            return trackedLeg.getLeader(getTimePoint(), cache);
        }
    }
    
    public class RankingInfoImpl extends AbstractRankingInfo {
        private static final long serialVersionUID = -2390284312153324336L;

        /**
         * The basic information for each competitor, telling about actual and corrected times as well as information
         * about actual and corrected times needed to reach the position of the competitor farthest ahead at
         * {@link #timePoint}.
         */
        private final Function<Competitor, RankingMetric.CompetitorRankingInfo> competitorRankingInfo;
        
        private final Competitor competitorFarthestAhead;
        
        /**
         * The competitor with the least corrected time for her arrival at {@link #competitorFarthestAhead}'s windward
         * position at {@link #timePoint}.
         */
        private final Competitor leaderByCorrectedEstimatedTimeToCompetitorFarthestAhead;
        
        public RankingInfoImpl(TimePoint timePoint, Map<Competitor, RankingMetric.CompetitorRankingInfo> competitorRankingInfo, Competitor competitorFarthestAhead) {
            super(timePoint);
            final Comparator<Duration> durationComparatorNullsLast = Comparator.nullsLast(Comparator.naturalOrder());
            this.competitorRankingInfo = c->competitorRankingInfo.get(c); 
            this.competitorFarthestAhead = competitorFarthestAhead;
            leaderByCorrectedEstimatedTimeToCompetitorFarthestAhead = competitorRankingInfo.keySet().stream().sorted(
                    (c1, c2) -> durationComparatorNullsLast.compare(
                            competitorRankingInfo.get(c1).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead(),
                            competitorRankingInfo.get(c2).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead())).
                      findFirst().orElse(null);
        }

        @Override
        public Function<Competitor, CompetitorRankingInfo> getCompetitorRankingInfo() {
            return competitorRankingInfo;
        }

        @Override
        public Competitor getCompetitorFarthestAhead() {
            return competitorFarthestAhead;
        }

        @Override
        public Competitor getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead() {
            return leaderByCorrectedEstimatedTimeToCompetitorFarthestAhead;
        }
    }

    protected AbstractRankingMetric(TrackedRace trackedRace) {
        super();
        this.trackedRace = trackedRace;
    }

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    /**
     * The time from the {@link TrackedRace#getStartOfRace() race start} until <code>timePoint</code> or until
     * the point in time when <code>competitor</code> passed the finish mark, whichever comes first.
     */
    @Override
    public Duration getActualTimeSinceStartOfRace(Competitor competitor, TimePoint timePoint) {
        final Duration result;
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        if (startOfRace == null) {
            result = null;
        } else {
            final Waypoint finish = getTrackedRace().getRace().getCourse().getLastWaypoint();
            if (finish == null) {
                result = null;
            } else {
                final MarkPassing finishingMarkPassing = getTrackedRace().getMarkPassing(competitor, finish);
                if (finishingMarkPassing != null && finishingMarkPassing.getTimePoint().before(timePoint)) {
                    result = startOfRace.until(finishingMarkPassing.getTimePoint());
                } else {
                    result = startOfRace.until(timePoint);
                }
            }
        }
        return result;
    }
    
    /**
     * Constructs a comparator based on the results of
     * {@link #getWindwardDistanceTraveled(Competitor, TimePoint, WindLegTypeAndLegBearingCache)} where competitors are
     * "less" than other competitors ("better") if they are in a later leg or, if in the same leg, have a greater
     * windward distance traveled. If both competitors have already finished the race, the finishing time is compared.
     */
    private Comparator<Competitor> getWindwardDistanceTraveledComparator(final TimePoint timePoint, final WindLegTypeAndLegBearingCache cache) {
        final Map<Competitor, Distance> windwardDistanceTraveledPerCompetitor = new HashMap<>();
        for (final Competitor competitor : getCompetitors()) {
            windwardDistanceTraveledPerCompetitor.put(competitor, getWindwardDistanceTraveled(competitor, timePoint, cache));
        }
        return new AbstractRaceRankComparator<Distance>(trackedRace, timePoint, /* lessIsBetter */ false) {
            @Override
            protected Distance getComparisonValueForSameLeg(Competitor competitor) {
                return windwardDistanceTraveledPerCompetitor.get(competitor);
            }
        };
    }

    /**
     * Fetches the competitors to consider for this ranking
     */
    protected Iterable<Competitor> getCompetitors() {
        return getTrackedRace().getRace().getCompetitors();
    }
    
    public RankingMetric.RankingInfo getRankingInfo(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        Map<Competitor, RankingMetric.CompetitorRankingInfo> result = new HashMap<>();
        Comparator<Competitor> oneDesignComparator = getWindwardDistanceTraveledComparator(timePoint, cache);
        Competitor competitorFarthestAhead = StreamSupport
                .stream(getCompetitors().spliterator(), /* parallel */true).
                sorted(oneDesignComparator).findFirst().get();
        final Distance totalWindwardDistanceTraveled = getWindwardDistanceTraveled(competitorFarthestAhead, timePoint, cache);
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        final Duration actualRaceDuration;
        if (startOfRace == null) {
            actualRaceDuration = null;
        } else {
            actualRaceDuration = startOfRace.until(timePoint);
            for (Competitor competitor : getCompetitors()) {
                final Duration predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead = getPredictedDurationToReachWindwardPositionOf(
                        competitor, competitorFarthestAhead, timePoint, cache);
                final Duration totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead = predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead == null ? null
                        : actualRaceDuration.plus(predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead);
                final Duration calculatedEstimatedTimeWhenReachingCompetitorFarthestAhead = totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead == null ? null
                        : getCalculatedTime(
                                competitor,
                                () -> getTrackedRace().getTrackedLeg(competitorFarthestAhead, timePoint).getLeg(),
                                () -> getTrackedRace().getTrack(competitorFarthestAhead).getEstimatedPosition(
                                        timePoint, /* extrapolate */true),
                                totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead,
                                totalWindwardDistanceTraveled);
                final Duration calculatedTime = getCalculatedTime(competitor,
                        () -> getTrackedRace().getCurrentLeg(competitor, timePoint).getLeg(), () -> getTrackedRace()
                                .getTrack(competitor).getEstimatedPosition(timePoint, /* extrapolated */true),
                        actualRaceDuration, totalWindwardDistanceTraveled);
                RankingMetric.CompetitorRankingInfo rankingInfo = new CompetitorRankingInfoImpl(timePoint, competitor,
                        getWindwardDistanceTraveled(competitor, timePoint, cache), actualRaceDuration, calculatedTime,
                        predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead,
                        calculatedEstimatedTimeWhenReachingCompetitorFarthestAhead);
                result.put(competitor, rankingInfo);
            }
        }
        return new RankingInfoImpl(timePoint, result, competitorFarthestAhead);
    }
    
    protected Comparator<Competitor> getComparatorByEstimatedCorrectedTimeWhenReachingCompetitorFarthestAhead(TimePoint timePoint) {
        return getComparatorByEstimatedCorrectedTimeWhenReachingCompetitorFarthestAhead(
                getRankingInfo(timePoint, new LeaderboardDTOCalculationReuseCache(timePoint)).getCompetitorRankingInfo());
    }

    protected Comparator<Competitor> getComparatorByEstimatedCorrectedTimeWhenReachingCompetitorFarthestAhead(
            final Function<Competitor, RankingMetric.CompetitorRankingInfo> rankingInfos) {
        return (c1, c2) -> rankingInfos.apply(c1).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead()
                .compareTo(rankingInfos.apply(c2).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead());
    }
    
    /**
     * Not all implementations may need the leg and the estimated position; therefore, to avoid unnecessary
     * calculations, {@link Supplier}s are expected instead of the values themselves, allowing for lazy on-demand
     * calculation.
     * 
     * @param estimatedPosition
     *            the position where the competitor <code>who</code> is when calculating the corrected time; some
     *            ranking metrics may require this information to determine quickly how far within the current leg the
     *            competitor has sailed. As others may not need it at all, the parameter is declared as a
     *            {@link Supplier} which delays evaluation until it is needed or avoids it altogether.
     */
    protected abstract Duration getCalculatedTime(Competitor who, Supplier<Leg> leg,
            Supplier<Position> estimatedPosition, Duration totalDurationSinceRaceStart,
            Distance totalWindwardDistanceTraveled);

    /**
     * Predicts how long <code>who</code> will take to reach competitor <code>to</code>'s position at
     * <code>timePoint</code>, starting at <code>who</code>'s position at <code>timePoint</code>, assuming a continued
     * performance for <code>who</code> that matches her average VMG on her current leg so far, and equal performance
     * with <code>to</code> on any subsequent leg that <code>who</code> needs to travel to reach <code>to</code>'s
     * position at <code>timePoint</code>. If <code>to</code> has already finished the race, the finish line position is
     * where <code>who</code> needs to arrive.
     * <p>
     * If <code>to</code> is already in a later leg, <code>who</code>'s remaining duration to reach the end of her
     * current leg is estimated using
     * {@link TrackedLegOfCompetitor#getEstimatedTimeToNextMark(TimePoint, com.sap.sailing.domain.tracking.WindPositionMode)}
     * ; then from the waypoint reached this way the
     * {@link #getAbsoluteWindwardDistanceTraveled(Competitor, Waypoint, TimePoint) windward distance to competitor
     * <code>to</code>} is determined and from this, using the handicaps for both competitors, <code>who</code> and
     * <code>to</code>, their performance between the waypoint and <code>to</code>'s position at <code>timePoint</code>
     * is equated, hence assuming that considering their handicaps, both competitors are doing equally well on this part
     * of the course, meaning that <code>who</code> will not gain any (corrected) time on <code>to</code> during this
     * period. From the equations, the duration it will take <code>who</code> to reach this position starting at the
     * upcoming waypoint can be determined which is then added to the duration estimated to reach that upcoming waypoint
     * (based on <code>who</code>'s average VMG in her current leg).
     * <p>
     * 
     * If <code>who</code> and <code>to</code> are in the same leg, the windward distance is calculated, and
     * <code>who</code>'s average VMG during the current leg is used to estimate the time until she reaches the position
     * <code>to</code> had at <code>timePoint</code>.
     * 
     * Precondition: <code>who</code>'s windward / along-course position is behind that of <code>to</code>, or an
     * {@link IllegalArgumentException} will be thrown.
     * <p>
     * 
     * @return <code>null</code>, if either of the two competitors' current legs is <code>null</code>
     */
    protected Duration getPredictedDurationToReachWindwardPositionOf(Competitor who, Competitor to, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final TrackedLegOfCompetitor currentLegWho = getCurrentLegOrLastLegIfAlreadyFinished(who, timePoint);
        final TrackedLegOfCompetitor currentLegTo = getCurrentLegOrLastLegIfAlreadyFinished(to, timePoint);
        final Duration result = getPredictedDurationToReachWindwardPositionOf(currentLegWho, currentLegTo, timePoint, cache);
        return result;
    }

    /**
     * Similar to
     * {@link #getPredictedDurationToReachWindwardPositionOf(Competitor, Competitor, TimePoint, WindLegTypeAndLegBearingCache)},
     * allowing the caller to specify the legs to consider for the two competitors. This way, the "to" competitor's
     * leg may be set to one that is not necessarily the current leg at <code>timePoint</code>, enabling a comparison
     * for a specific leg.
     * <p>
     * 
     * The resulting duration may be negative if <code>legWho</code>'s competitor has reached the position in question
     * before <code>timePoint</code>.
     * <p>
     * 
     * Precondition: <code>legWho</code>'s leg is the same as or an earlier leg than <code>legTo</code>'s leg.
     */
    protected Duration getPredictedDurationToReachWindwardPositionOf(final TrackedLegOfCompetitor legWho, final TrackedLegOfCompetitor legTo,
            TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        assert legWho == null || legTo == null ||
                getTrackedRace().getRace().getCourse().getIndexOfWaypoint(legWho.getTrackedLeg().getLeg().getFrom()) <=
                getTrackedRace().getRace().getCourse().getIndexOfWaypoint(legTo.getTrackedLeg().getLeg().getFrom());
        final Duration result;
        if (legWho == null || legTo == null || !legWho.hasStartedLeg(timePoint) || !legTo.hasStartedLeg(timePoint)) {
            result = null;
        } else {
            final Competitor who = legWho.getCompetitor();
            final Competitor to = legTo.getCompetitor();
            if (who == to) {
                // the same competitor requires no time to reach its own position; it's already there;
                // however, if the competitor has already finished the leg at or before timePoint, the duration will
                // have to be negative, even if who==to
                if (legWho.hasFinishedLeg(timePoint)) {
                    result = timePoint.until(legWho.getFinishTime());
                } else {
                    result = Duration.NULL;
                }
            } else {
                assert getTrackedRace().getRace().getCourse().getIndexOfWaypoint(legWho.getLeg().getFrom()) <= getTrackedRace()
                        .getRace().getCourse().getIndexOfWaypoint(legTo.getLeg().getFrom());
                final Duration toEndOfLegOrTo = getPredictedDurationToEndOfLegOrTo(timePoint, legWho, legWho.getTrackedLeg().getTrackedLeg(to), cache);
                if (toEndOfLegOrTo == null) {
                    result = null;
                } else {
                    final Duration durationForSubsequentLegsToReachAtEqualPerformance;
                    if (legWho.getLeg() == legTo.getLeg()) {
                        durationForSubsequentLegsToReachAtEqualPerformance = Duration.NULL;
                    } else {
                        durationForSubsequentLegsToReachAtEqualPerformance = getDurationToReachAtEqualPerformance(who, to,
                                legWho.getLeg().getTo(), timePoint, cache);
                    }
                    result = durationForSubsequentLegsToReachAtEqualPerformance == null ? null : toEndOfLegOrTo.plus(durationForSubsequentLegsToReachAtEqualPerformance);
                }
            }
        }
        return result;
    }

    /**
     * Get's <code>who</code>'s current tracked leg at <code>timePoint</code>, or <code>null</code> if <code>who</code> hasn't
     * started at <code>timePoint</code> yet, or <code>who</code>'s tracked leg for the last leg if <code>who</code> has
     * already finished the race at <code>timePoint</code> or 
     */
    protected TrackedLegOfCompetitor getCurrentLegOrLastLegIfAlreadyFinished(Competitor who, TimePoint timePoint) {
        TrackedLegOfCompetitor currentLegWho = getTrackedRace().getCurrentLeg(who, timePoint);
        if (currentLegWho == null) { // already finished or not yet started; if already finished, use last leg
            final Waypoint lastWaypoint = getTrackedRace().getRace().getCourse().getLastWaypoint();
            if (lastWaypoint != null && getTrackedRace().getRace().getCourse().getNumberOfWaypoints() > 1) { // could be an empty course
                final TrackedLeg lastTrackedLeg = getTrackedRace().getTrackedLegFinishingAt(lastWaypoint);
                TrackedLegOfCompetitor whosLastTrackedLeg = lastTrackedLeg.getTrackedLeg(who);
                if (whosLastTrackedLeg.hasFinishedLeg(timePoint)) {
                    currentLegWho = whosLastTrackedLeg;
                }
            }
        }
        return currentLegWho;
    }

    /**
     * For the situation at <code>timePoint</code>, determines how long in real, uncorrected time <code>who</code> lags
     * behind <code>to</code> in the leg identified by <code>legWho</code>. If both are still sailing in the leg at
     * <code>timePoint</code>, this is the time <code>who</code> needs with constant average VMG to reach
     * <code>to</code>'s position at <code>timePoint</code>. If only <code>to</code> has already finished the leg and no
     * mark passing time is known yet for <code>who</code> for the end of the leg then <code>who</code> is projected to
     * the end of the leg using her average VMG on the leg, and the difference between <code>who</code>'s projected and
     * <code>to</code>'s actual mark passing times is returned.
     * <p>
     * 
     * If leg finish mark passings are available for both, <code>who</code> and <code>to</code>, the difference between
     * them is returned.
     * <p>
     * 
     * The result may be a negative duration in case <code>who</code> reached the position in question before
     * <code>timePoint</code>.<p>
     * 
     * Precondition: <code>who</code> and <code>to</code> have both started sailing the leg at <code>timePoint</code>
     * and <code>to</code> has sailed a greater or equal windward distance compared to <code>who</code>. If not, the
     * result is undefined.
     */
    protected Duration getPredictedDurationToEndOfLegOrTo(TimePoint timePoint, final TrackedLegOfCompetitor legWho, final TrackedLegOfCompetitor legTo,
            WindLegTypeAndLegBearingCache cache) {
        assert legWho.hasStartedLeg(timePoint);
        assert legTo.hasStartedLeg(timePoint);
        final Duration toEndOfLegOrTo;
        if (legTo.hasFinishedLeg(timePoint)) {
            // calculate actual time it takes who to reach the end of the leg starting at timePoint:
            final TimePoint whosLegFinishTime = legWho.getFinishTime();
            if (whosLegFinishTime != null && !whosLegFinishTime.after(timePoint)) {
                // who's leg finishing time is known and is already reached at timePoint; we don't need to extrapolate
                toEndOfLegOrTo = timePoint.until(whosLegFinishTime);
            } else {
                assert getWindwardDistanceTraveled(legTo.getCompetitor(), legTo.hasFinishedLeg(timePoint)?legTo.getFinishTime():timePoint, cache).compareTo(
                        getWindwardDistanceTraveled(legWho.getCompetitor(), legWho.hasFinishedLeg(timePoint)?legWho.getFinishTime():timePoint, cache)) >= 0;
                // estimate who's leg finishing time by extrapolating with the average VMG (if available) or the current VMG
                // (if no average VMG can currently be computed, e.g., because the time point is exactly at the leg start)
                final Position windwardPositionToReachInWhosCurrentLeg =
                                getTrackedRace().getApproximatePosition(legWho.getLeg().getTo(), timePoint);
                toEndOfLegOrTo = getDurationToReach(windwardPositionToReachInWhosCurrentLeg, timePoint, legWho, cache);
            }
        } else {
            // competitor "to" is still in same leg; project "who" to "to"'s position using VMG
            final Position positionOfTo = getTrackedRace().getTrack(legTo.getCompetitor()).getEstimatedPosition(timePoint, /* extrapolate */ true);
            toEndOfLegOrTo = getDurationToReach(positionOfTo, timePoint, legWho, cache);
        }
        return toEndOfLegOrTo;
    }

    private Duration getDurationToReach(final Position windwardPositionToReachInWhosCurrentLeg, TimePoint timePoint,
            final TrackedLegOfCompetitor whosLeg, WindLegTypeAndLegBearingCache cache) {
        final Duration toEndOfLegOrTo;
        final Speed averageVMG = whosLeg.getAverageVelocityMadeGood(timePoint, cache);
        final Speed vmg = averageVMG == null || Double.isNaN(averageVMG.getKnots()) ?
                /* default to current VMG */ whosLeg.getVelocityMadeGood(timePoint, WindPositionMode.EXACT, cache) : averageVMG;
        toEndOfLegOrTo = vmg == null || vmg.getKnots() == 0.0 ? null : vmg.getDuration(
                whosLeg.getTrackedLeg().getWindwardDistance(
                        getTrackedRace().getTrack(whosLeg.getCompetitor()).getEstimatedPosition(timePoint, /* extrapolate */true),
                        windwardPositionToReachInWhosCurrentLeg, timePoint, WindPositionMode.LEG_MIDDLE));
        return toEndOfLegOrTo;
    }
    
    /**
     * Computes the duration that <code>who</code> would take to reach <code>to</code>'s windward / along-track position
     * at <code>timePoint</code>, starting at <code>fromWaypoint</code>, assuming the same corrected performance at
     * which <code>to</code> sailed starting at <code>fromWaypoint</code> up to her current position.
     * <p>
     * 
     * Precondition: competitor <code>to</code> has already passed <code>fromWaypoint</code>. If not, an
     * {@link IllegalArgumentException} will be thrown.
     * <p>
     * 
     * Implementations can validate this precondition using
     * {@link #validateGetDurationToReachAtEqualPerformanceParameters(Competitor, Waypoint, TimePoint, MarkPassing)}.
     */
    protected abstract Duration getDurationToReachAtEqualPerformance(Competitor who, Competitor to, Waypoint fromWaypoint,
            TimePoint timePointOfTosPosition, WindLegTypeAndLegBearingCache cache);
    
    protected void validateGetDurationToReachAtEqualPerformanceParameters(Competitor to, Waypoint fromWaypoint,
            TimePoint timePointOfTosPosition, final MarkPassing whenToPassedFromWaypoint) {
        if (whenToPassedFromWaypoint == null) {
            throw new IllegalArgumentException("Competitor "+to+" is expected to have passed "+fromWaypoint+" but hasn't");
        }
        if (whenToPassedFromWaypoint.getTimePoint().after(timePointOfTosPosition)) {
            throw new IllegalArgumentException("Competitor was expected to have passed "+fromWaypoint+" before "+timePointOfTosPosition+
                    " but did pass it at "+whenToPassedFromWaypoint.getTimePoint());
        }
    }

    /**
     * How far did <code>competitor</code> sail windwards/along-course since the start of the race?
     * For each leg the total windward distance sailed is limited to the leg's windward distance at its
     * {@link TrackedLeg#getReferenceTimePoint() reference time point}. This ensures that significantly "overstaying" the lay lines
     * doesn't let a competitor rank better than one who already passed the mark but traveled little windward distance in
     * the next leg.
     * 
     * @param timePoint needed to determine <code>competitor</code>'s position at that time point; note that the
     * time point for wind approximation is taken to be a reference time point selected based on the mark passings
     * for the respective leg's from/to waypoints.
     */
    protected Distance getWindwardDistanceTraveled(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return getWindwardDistanceTraveled(competitor, getTrackedRace().getRace().getCourse().getFirstWaypoint(), timePoint, cache);
    }

    /**
     * How far did <code>competitor</code> sail windwards/along-course since passing the <code>from</code> waypoint?
     * For each leg the total windward distance sailed is limited to the leg's windward distance at its
     * {@link TrackedLeg#getReferenceTimePoint() reference time point}. This ensures that significantly "overstaying" the lay lines
     * doesn't let a competitor rank better than one who already passed the mark but traveled little windward distance in
     * the next leg.<p>
     * 
     * If mark positions along the way are not known, the windward distance of those legs will be counted as 0.
     * 
     * @param timePoint needed to determine <code>competitor</code>'s position at that time point; note that the
     * time point for wind approximation is taken to be a reference time point selected based on the mark passings
     * for the respective leg's from/to waypoints.
     */
    protected Distance getWindwardDistanceTraveled(Competitor competitor, Waypoint from, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final Distance result;
        if (from == null) {
            result = null;
        } else {
            Distance d = Distance.NULL;
            boolean count = false; // start counting only once the "from" waypoint has been found
            final Course course = getTrackedRace().getRace().getCourse();
            course.lockForRead();
            try {
                for (final TrackedLeg trackedLeg : getTrackedRace().getTrackedLegs()) {
                    count = count || trackedLeg.getLeg().getFrom() == from;
                    if (count) {
                        final TrackedLegOfCompetitor trackedLegOfCompetitor = trackedLeg.getTrackedLeg(competitor);
                        if (trackedLegOfCompetitor.hasStartedLeg(timePoint)) {
                            if (!trackedLegOfCompetitor.hasFinishedLeg(timePoint)) {
                                // partial distance sailed:
                                final Position estimatedPosition = getTrackedRace().getTrack(competitor).getEstimatedPosition(timePoint, /* extrapolate */ true);
                                if (estimatedPosition != null) {
                                    final Distance windwardDistanceFromLegStart = trackedLeg.getWindwardDistanceFromLegStart(estimatedPosition, cache);
                                    if (windwardDistanceFromLegStart == null) {
                                        // probably the leg start position is not known; therefore, distance cannot be determined; return null:
                                        d = null;
                                        break;
                                    }
                                    final Distance legWindwardDistance = trackedLeg.getWindwardDistance(cache);
                                    if (legWindwardDistance != null && legWindwardDistance.compareTo(windwardDistanceFromLegStart) < 0) {
                                        d = d.add(legWindwardDistance);
                                    } else {
                                        // if the competitor is currently at the mark rounding, the windward distance within the leg may
                                        // be negative; don't reduce the distance in this case
                                        if (windwardDistanceFromLegStart.getMeters() > 0) {
                                            d = d.add(windwardDistanceFromLegStart);
                                        }
                                    }
                                }
                                break;
                            } else {
                                final Distance legWindwardDistance = trackedLeg.getWindwardDistance(cache);
                                if (legWindwardDistance != null) {
                                    d = d.add(legWindwardDistance);
                                }
                            }
                        }
                    }
                }
            } finally {
                course.unlockAfterRead();
            }
            result = d;
        }
        return result;
    }

    /**
     * @return <code>null</code> if no competitor has started the leg yet; the first competitor to finish the leg if any
     *         has already finished the leg at <code>timePoint</code>; or the competitor with the greatest windward
     *         distance traveled in the leg at <code>timePoint</code> otherwise
     */
    protected Competitor getCompetitorFarthestAheadInLeg(TrackedLeg trackedLeg, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        Competitor firstAroundMark = getFirstLegFinisherBefore(trackedLeg, timePoint);
        final Competitor result;
        if (firstAroundMark != null) {
            result = firstAroundMark;
        } else {
            List<MarkPassing> copyOfMarkPassingsForLegStart = new ArrayList<>();
            {   // scope the markPassingsForLegStart, so it is no longer used outside this block
                final Iterable<MarkPassing> markPassingsForLegStart = getTrackedRace().getMarkPassingsInOrder(trackedLeg.getLeg().getFrom());
                // See bug 3728: obtaining lock for mark passings in order for a waypoint before code potentially called from here,
                // e.g., through getWindwardDistanceTraveled(...), tries to obtain the read lock for mark passings for a competitor can
                // result in a deadlock. Therefore, we copy the mark passings for the leg start under the lock, then release it again
                // before calling into a deep stack with getWindwardDistanceTraveled(...) which may well obtain a read lock on
                // the mark passings for a competitor.
                getTrackedRace().lockForRead(markPassingsForLegStart);
                try {
                    Util.addAll(markPassingsForLegStart, copyOfMarkPassingsForLegStart);
                } finally {
                    getTrackedRace().unlockAfterRead(markPassingsForLegStart);
                }
            }
            Distance maxWindwardDistanceTraveled = new MeterDistance(Double.MIN_VALUE);
            Competitor competitorFarthestAlong = null;
            for (MarkPassing mp : copyOfMarkPassingsForLegStart) {
                if (mp.getTimePoint().after(timePoint)) {
                    break;
                }
                final Distance windwardDistanceTraveled = getWindwardDistanceTraveled(mp.getCompetitor(), mp.getWaypoint(), timePoint, cache);
                if (windwardDistanceTraveled.compareTo(maxWindwardDistanceTraveled) > 0) {
                    maxWindwardDistanceTraveled = windwardDistanceTraveled;
                    competitorFarthestAlong = mp.getCompetitor();
                }
            }
            result = competitorFarthestAlong;
        }
        return result;
    }

    /**
     * Determines the first competitor finishing the leg identified by <code>trackedLeg</code> at or before <code>timePoint</code>. If
     * no such competitor exists, <code>null</code> is returned.
     */
    private Competitor getFirstLegFinisherBefore(TrackedLeg trackedLeg, TimePoint timePoint) {
        Iterable<MarkPassing> markPassingsForLegEnd = getTrackedRace().getMarkPassingsInOrder(trackedLeg.getLeg().getTo());
        Competitor firstAroundMark = null;
        getTrackedRace().lockForRead(markPassingsForLegEnd);
        try {
            final Iterator<MarkPassing> i = markPassingsForLegEnd.iterator();
            if (i.hasNext()) {
                MarkPassing markPassing = i.next();
                if (!markPassing.getTimePoint().after(timePoint)) {
                    firstAroundMark = markPassing.getCompetitor();
                }
            }
        } finally {
            getTrackedRace().unlockAfterRead(markPassingsForLegEnd);
        }
        return firstAroundMark;
    }
}
