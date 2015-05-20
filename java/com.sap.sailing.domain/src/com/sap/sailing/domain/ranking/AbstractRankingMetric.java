package com.sap.sailing.domain.ranking;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

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

    public class RankingInfoImpl implements RankingMetric.RankingInfo {
        private static final long serialVersionUID = -2390284312153324336L;

        /**
         * The time point for which this ranking information is valid
         */
        private final TimePoint timePoint;
        
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
            final Comparator<Duration> durationComparatorNullsLast = Comparator.nullsLast(Comparator.naturalOrder());
            this.timePoint = timePoint;
            this.competitorRankingInfo = c->competitorRankingInfo.get(c); 
            this.competitorFarthestAhead = competitorFarthestAhead;
            leaderByCorrectedEstimatedTimeToCompetitorFarthestAhead = competitorRankingInfo.keySet().stream().sorted(
                    (c1, c2) -> durationComparatorNullsLast.compare(
                            competitorRankingInfo.get(c1).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead(),
                            competitorRankingInfo.get(c2).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead())).
                      findFirst().get();
        }

        @Override
        public TimePoint getTimePoint() {
            return timePoint;
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

    protected Duration getActualTimeSinceStartOfRace(Competitor competitor, TimePoint timePoint) {
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
                if (finishingMarkPassing != null) {
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
     * "less" than other competitors ("better") if they have a greater windward distance traveled.
     */
    private Comparator<Competitor> getWindwardDistanceTraveledComparator(final TimePoint timePoint, final WindLegTypeAndLegBearingCache cache) {
        final Map<Competitor, Distance> windwardDistanceTraveledPerCompetitor = new HashMap<>();
        for (final Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            windwardDistanceTraveledPerCompetitor.put(competitor, getWindwardDistanceTraveled(competitor, timePoint, cache));
        }
        final Comparator<Distance> nullsFirstDistanceComparator = Comparator.nullsFirst(Comparator.naturalOrder());
        return (c1, c2) -> nullsFirstDistanceComparator.compare(
                windwardDistanceTraveledPerCompetitor.get(c2),
                windwardDistanceTraveledPerCompetitor.get(c1));
    }
    
    public RankingMetric.RankingInfo getRankingInfo(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        Map<Competitor, RankingMetric.CompetitorRankingInfo> result = new HashMap<>();
        Comparator<Competitor> oneDesignComparator = getWindwardDistanceTraveledComparator(timePoint, cache);
        Competitor competitorFarthestAhead = StreamSupport
                .stream(getTrackedRace().getRace().getCompetitors().spliterator(), /* parallel */true).
                sorted(oneDesignComparator).findFirst().get();
        final Distance totalWindwardDistanceTraveled = getWindwardDistanceTraveled(competitorFarthestAhead, timePoint, cache);
        final TimePoint startOfRace = getTrackedRace().getStartOfRace();
        final Duration actualRaceDuration;
        if (startOfRace == null) {
            actualRaceDuration = null;
        } else {
            actualRaceDuration = startOfRace.until(timePoint);
            for (Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
                final Duration predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead = getPredictedDurationToReachWindwardPositionOf(
                        competitor, competitorFarthestAhead, timePoint, cache);
                final Duration totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead = predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead == null ? null
                        : actualRaceDuration.plus(predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead);
                final Duration correctedEstimatedTimeWhenReachingCompetitorFarthestAhead = totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead == null ? null
                        : getCorrectedTime(
                                competitor,
                                () -> getTrackedRace().getTrackedLeg(competitorFarthestAhead, timePoint).getLeg(),
                                () -> getTrackedRace().getTrack(competitorFarthestAhead).getEstimatedPosition(
                                        timePoint, /* extrapolate */true),
                                totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead,
                                totalWindwardDistanceTraveled);
                final Duration correctedTime = getCorrectedTime(competitor,
                        () -> getTrackedRace().getCurrentLeg(competitor, timePoint).getLeg(), () -> getTrackedRace()
                                .getTrack(competitor).getEstimatedPosition(timePoint, /* extrapolated */true),
                        actualRaceDuration, totalWindwardDistanceTraveled);
                RankingMetric.CompetitorRankingInfo rankingInfo = new CompetitorRankingInfoImpl(timePoint, competitor,
                        getWindwardDistanceTraveled(competitor, timePoint, cache), actualRaceDuration, correctedTime,
                        predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead,
                        correctedEstimatedTimeWhenReachingCompetitorFarthestAhead);
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
    protected abstract Duration getCorrectedTime(Competitor who, Supplier<Leg> leg,
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
                // the same competitor requires no time to reach its own position; it's already there...
                // if legWho hasFinished at timePoint already, the duration will have to be negative, even if who==to
                if (legWho.hasFinishedLeg(timePoint)) {
                    result = legWho.getFinishTime().until(timePoint);
                } else {
                    result = Duration.NULL;
                }
            } else {
                assert getTrackedRace().getRace().getCourse().getIndexOfWaypoint(legWho.getLeg().getFrom()) <= getTrackedRace()
                        .getRace().getCourse().getIndexOfWaypoint(legTo.getLeg().getFrom());
                final Duration toEndOfLegOrTo = getPredictedDurationToEndOfLegOrTo(timePoint, legWho, legTo, cache);
                final Duration durationForSubsequentLegsToReachAtEqualPerformance;
                if (legWho.getLeg() == legTo.getLeg()) {
                    durationForSubsequentLegsToReachAtEqualPerformance = Duration.NULL;
                } else {
                    durationForSubsequentLegsToReachAtEqualPerformance = getDurationToReachAtEqualPerformance(who, to,
                            legWho.getLeg().getTo(), timePoint, cache);
                }
                result = toEndOfLegOrTo.plus(durationForSubsequentLegsToReachAtEqualPerformance);
            }
        }
        return result;
    }

    /**
     * Get's <code>who</code>'s current tracked leg at <code>timePoint</code>, or <code>null</code> if <code>who</code> hasn't
     * started at <code>timePoint</code> yet, or <code>who</code>'s tracked leg for the last leg if <code>who</code> has
     * already finished the race at <code>timePoint</code>.
     */
    private TrackedLegOfCompetitor getCurrentLegOrLastLegIfAlreadyFinished(Competitor who, TimePoint timePoint) {
        TrackedLegOfCompetitor currentLegWho = getTrackedRace().getCurrentLeg(who, timePoint);
        if (currentLegWho == null) { // already finished or not yet started; if already finished, use last leg
            final Waypoint lastWaypoint = getTrackedRace().getRace().getCourse().getLastWaypoint();
            final TrackedLeg lastTrackedLeg = getTrackedRace().getTrackedLegFinishingAt(lastWaypoint);
            TrackedLegOfCompetitor whosLastTrackedLeg = lastTrackedLeg.getTrackedLeg(who);
            if (whosLastTrackedLeg.hasFinishedLeg(timePoint)) {
                currentLegWho = whosLastTrackedLeg;
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
        assert getWindwardDistanceTraveled(legTo.getCompetitor(), legTo.hasFinishedLeg(timePoint)?legTo.getFinishTime():timePoint, cache).compareTo(
                getWindwardDistanceTraveled(legWho.getCompetitor(), legWho.hasFinishedLeg(timePoint)?legWho.getFinishTime():timePoint, cache)) >= 0;
        final Duration toEndOfLegOrTo;
        if (legTo.hasFinishedLeg(timePoint)) {
            // calculate actual time it takes who to reach the end of the leg starting at timePoint:
            final TimePoint whosLegFinishTime = legWho.getFinishTime();
            if (whosLegFinishTime != null) {
                // who's leg finishing time is known; we don't need to extrapolate
                toEndOfLegOrTo = timePoint.until(whosLegFinishTime);
            } else {
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
        final Speed currentVMG = whosLeg.getVelocityMadeGood(timePoint, WindPositionMode.EXACT, cache);
        final Speed averageVMG = whosLeg.getAverageVelocityMadeGood(timePoint, cache);
        final Speed vmg = Double.isNaN(averageVMG.getKnots()) ? currentVMG : averageVMG;
        toEndOfLegOrTo = vmg.getDuration(
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
     * the next leg.
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
            for (final TrackedLeg trackedLeg : getTrackedRace().getTrackedLegs()) {
                count = count || trackedLeg.getLeg().getFrom() == from;
                if (count) {
                    final TrackedLegOfCompetitor trackedLegOfCompetitor = trackedLeg.getTrackedLeg(competitor);
                    if (trackedLegOfCompetitor.hasStartedLeg(timePoint)) {
                        if (!trackedLegOfCompetitor.hasFinishedLeg(timePoint)) {
                            // partial distance sailed:
                            final Distance windwardDistanceFromLegStart = trackedLeg.getWindwardDistanceFromLegStart(
                                    getTrackedRace().getTrack(competitor).getEstimatedPosition(timePoint, /* extrapolate */ true), cache);
                            final Distance legWindwardDistance = trackedLeg.getWindwardDistance(cache);
                            if (legWindwardDistance.compareTo(windwardDistanceFromLegStart) < 0) {
                                d = d.add(legWindwardDistance);
                            } else {
                                d = d.add(windwardDistanceFromLegStart);
                            }
                            break;
                        } else {
                            d = d.add(trackedLeg.getWindwardDistance(cache));
                        }
                    }
                }
            }
            result = d;
        }
        return result;
    }
}
