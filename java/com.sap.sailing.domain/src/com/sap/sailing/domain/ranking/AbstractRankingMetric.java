package com.sap.sailing.domain.ranking;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.impl.NoCachingWindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.impl.RaceRankComparator;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public abstract class AbstractRankingMetric implements RankingMetric {
    private static final long serialVersionUID = -3671039530564696392L;
    private final TrackedRace trackedRace;
    
    public class CompetitorRankingInfo implements Serializable {
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

        protected CompetitorRankingInfo(TimePoint timePoint, Competitor competitor, Distance windwardDistanceSailed,
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

        public TimePoint getTimePoint() {
            return timePoint;
        }

        public Competitor getCompetitor() {
            return competitor;
        }

        public Distance getWindwardDistanceSailed() {
            return windwardDistanceSailed;
        }

        public Duration getActualTime() {
            return actualTime;
        }

        public Duration getCorrectedTime() {
            return correctedTime;
        }

        public Duration getEstimatedActualDurationFromTimePointToCompetitorFarthestAhead() {
            return estimatedActualDurationToCompetitorFarthestAhead;
        }
        
        public Duration getEstimatedActualDurationFromRaceStartToCompetitorFarthestAhead() {
            return getTrackedRace().getStartOfRace().until(getTimePoint()).plus(getEstimatedActualDurationFromTimePointToCompetitorFarthestAhead());
        }

        public Duration getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead() {
            return correctedTimeAtEstimatedArrivalAtCompetitorFarthestAhead;
        }
    }
    
    public class RankingInfo implements Serializable {
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
        private final Map<Competitor, CompetitorRankingInfo> competitorRankingInfo;
        
        private final Competitor competitorFarthestAhead;
        
        /**
         * The competitor with the least corrected time for her arrival at {@link #competitorFarthestAhead}'s windward
         * position at {@link #timePoint}.
         */
        private final Competitor leaderByCorrectedEstimatedTimeToCompetitorFarthestAhead;
        
        public RankingInfo(TimePoint timePoint, Map<Competitor, CompetitorRankingInfo> competitorRankingInfo, Competitor competitorFarthestAhead) {
            this.timePoint = timePoint;
            this.competitorRankingInfo = competitorRankingInfo; 
            this.competitorFarthestAhead = competitorFarthestAhead;
            leaderByCorrectedEstimatedTimeToCompetitorFarthestAhead = competitorRankingInfo.keySet().stream().sorted(
                    (c1, c2) -> competitorRankingInfo.get(c1).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead().
                      compareTo(competitorRankingInfo.get(c2).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead())).findFirst().get();
        }

        public TimePoint getTimePoint() {
            return timePoint;
        }

        public Map<Competitor, CompetitorRankingInfo> getCompetitorRankingInfo() {
            return competitorRankingInfo;
        }

        public Competitor getCompetitorFarthestAhead() {
            return competitorFarthestAhead;
        }

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
    
    protected RankingInfo getRankingInfo(TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        Map<Competitor, CompetitorRankingInfo> result = new HashMap<>();
        RaceRankComparator oneDesignComparator = new RaceRankComparator(getTrackedRace(), timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
        Competitor competitorFarthestAhead = StreamSupport
                .stream(getTrackedRace().getRace().getCompetitors().spliterator(), /* parallel */true).
                sorted(oneDesignComparator).findFirst().get();
        final Distance totalWindwardDistanceTraveled = getWindwardDistanceTraveled(competitorFarthestAhead, timePoint);
        final Duration actualRaceDuration = getTrackedRace().getStartOfRace().until(timePoint);
        for (Competitor competitor : getTrackedRace().getRace().getCompetitors()) {
            final Duration predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead =
                    getPredictedDurationToReachWindwardPositionOf(competitor, competitorFarthestAhead, timePoint);
            final Duration totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead =
                    actualRaceDuration.plus(predictedDurationToReachWindwardPositionOfCompetitorFarthestAhead);
            final Duration correctedEstimatedTimeWhenReachingCompetitorFarthestAhead = getCorrectedTime(competitor,
                    ()->getTrackedRace().getTrackedLeg(competitorFarthestAhead, timePoint).getLeg(),
                    ()->getTrackedRace().getTrack(competitorFarthestAhead).getEstimatedPosition(timePoint, /* extrapolate */ true),
                    totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead, totalWindwardDistanceTraveled);
            final Duration correctedTime = getCorrectedTime(competitor,
                    ()->getTrackedRace().getCurrentLeg(competitor, timePoint).getLeg(),
                    ()->getTrackedRace().getTrack(competitor).getEstimatedPosition(timePoint, /* extrapolated */ true),
                    actualRaceDuration, totalWindwardDistanceTraveled);
            CompetitorRankingInfo rankingInfo = new CompetitorRankingInfo(timePoint, competitor, getWindwardDistanceTraveled(competitor, timePoint),
                    actualRaceDuration, correctedTime, totalEstimatedDurationSinceRaceStartToCompetitorFarthestAhead, correctedEstimatedTimeWhenReachingCompetitorFarthestAhead);
            result.put(competitor, rankingInfo);
        }
        return new RankingInfo(timePoint, result, competitorFarthestAhead);
    }
    
    protected Comparator<Competitor> getComparatorByEstimatedCorrectedTimeWhenReachingCompetitorFarthestAhead(TimePoint timePoint) {
        return getComparatorByEstimatedCorrectedTimeWhenReachingCompetitorFarthestAhead(
                getRankingInfo(timePoint, new NoCachingWindLegTypeAndLegBearingCache()).getCompetitorRankingInfo());
    }

    protected Comparator<Competitor> getComparatorByEstimatedCorrectedTimeWhenReachingCompetitorFarthestAhead(final Map<Competitor, CompetitorRankingInfo> rankingInfos) {
        return (c1, c2) -> rankingInfos.get(c1).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead()
                .compareTo(rankingInfos.get(c2).getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead());
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
     * position at <code>timePoint</code>. If <code>to</code> has already finished the race, the finish line position
     * is where <code>who</code> needs to arrive.
     * <p>
     * If <code>to</code> is already in a later leg, <code>who</code>'s remaining duration to reach the end of her
     * current leg is estimated using
     * {@link TrackedLegOfCompetitor#getEstimatedTimeToNextMark(TimePoint, com.sap.sailing.domain.tracking.WindPositionMode)}
     * ; then from the waypoint reached this way the
     * {@link #getWindwardDistanceTraveled(Competitor, Waypoint, TimePoint) windward distance to competitor
     * <code>to</code>} is determined and from this, using the handicaps for both competitors, <code>who</code> and
     * <code>to</code>, their performance between the waypoint and <code>to</code>'s position at <code>timePoint</code>
     * is equated, hence assuming that considering their handicaps, both competitors are doing equally well on this part
     * of the course, meaning that <code>who</code> will not gain any time on <code>to</code> during this period. From
     * the equations, the duration it will take <code>who</code> to reach this position starting at the upcoming
     * waypoint can be determined which is then added to the duration estimated to reach that upcoming waypoint (based
     * on <code>who</code>'s average VMG in her current leg).
     * <p>
     * 
     * If <code>who</code> and <code>to</code> are in the same leg, the windward distance is calculated, and
     * <code>who</code>'s average VMG during the current leg is used to estimate the time until she reaches the position
     * <code>to</code> had at <code>timePoint</code>.
     * 
     * Precondition: <code>who</code>'s windward / along-course position is behind that of <code>to</code>, or an
     * {@link IllegalArgumentException} will be thrown.<p>
     * 
     * @return <code>null</code>, if either of the two competitors' current legs is <code>null</code>
     */
    protected Duration getPredictedDurationToReachWindwardPositionOf(Competitor who, Competitor to, TimePoint timePoint) {
        final TrackedLegOfCompetitor currentLegWho = getTrackedRace().getCurrentLeg(who, timePoint);
        final TrackedLegOfCompetitor currentLegTo = getTrackedRace().getCurrentLeg(to, timePoint);
        assert getTrackedRace().getRace().getCourse().getIndexOfWaypoint(currentLegWho.getLeg().getFrom()) <=
                getTrackedRace().getRace().getCourse().getIndexOfWaypoint(currentLegTo.getLeg().getFrom());
        final Duration result;
        if (who == to) { // the same competitor requires no time to reach its own position; it's already there...
            result = Duration.NULL;
        } else if (currentLegWho == null || currentLegTo == null) {
            result = null;
        } else {
            final Duration toEndOfLegOrTo = getPredictedDurationToEndOfLegOrTo(who, to, timePoint, currentLegWho, currentLegTo);
            final Duration durationForSubsequentLegsToReachAtEqualPerformance;
            if (currentLegWho.getLeg() == currentLegTo.getLeg()) {
                durationForSubsequentLegsToReachAtEqualPerformance = Duration.NULL;
            } else {
                durationForSubsequentLegsToReachAtEqualPerformance = getDurationToReachAtEqualPerformance(who, to, currentLegWho.getLeg().getTo(), timePoint);
            }
            result = toEndOfLegOrTo.plus(durationForSubsequentLegsToReachAtEqualPerformance);
        }
        return result;
    }

    /**
     * For the situation at <code>timePoint</code>, determines how long in real, uncorrected time <code>who</code> will
     * need, given her current leg's average VMG, to reach <code>to</code> if <code>to</code> is in the same leg, or to
     * reach the end of the leg if <code>to</code> has already completed the leg.
     * <p>
     * 
     * Precondition: <code>to</code> has sailed a greater or equal windward distance compared to <code>who</code>. If
     * not, the result is undefined.
     */
    protected Duration getPredictedDurationToEndOfLegOrTo(Competitor who, Competitor to, TimePoint timePoint,
            final TrackedLegOfCompetitor currentLegWho, final TrackedLegOfCompetitor currentLegTo) {
        assert getWindwardDistanceTraveled(to, timePoint).compareTo(getWindwardDistanceTraveled(who, timePoint)) >= 0;
        final Position windwardPositionToReachInWhosCurrentLeg =
                (currentLegWho.getLeg() == currentLegTo.getLeg())
                        // both are currently in the same leg; estimate who's arrival at to's current windward position
                        // using who's average VMG in current leg
                        ? getTrackedRace().getTrack(to).getEstimatedPosition(timePoint, /* extrapolate */ true)
                        // not in the same leg; let "who" travel to the end of the leg
                        : getTrackedRace().getApproximatePosition(currentLegWho.getLeg().getTo(), timePoint);
        final Duration toEndOfLegOrTo = currentLegWho.getAverageVelocityMadeGood(timePoint).getDuration(
                currentLegWho.getTrackedLeg().getWindwardDistance(
                        getTrackedRace().getTrack(who).getEstimatedPosition(timePoint, /* extrapolate */true),
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
            TimePoint timePointOfTosPosition);
    
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

    protected Distance getWindwardDistanceTraveled(Competitor competitor, TimePoint timePoint) {
        return getWindwardDistanceTraveled(competitor, getTrackedRace().getRace().getCourse().getFirstWaypoint(), timePoint);
    }
    
    /**
     * How far did <code>competitor</code> sail windwards/along-course since passing the <code>from</code> waypoint?
     * 
     * @param timePoint needed to determine <code>competitor</code>'s position at that time point; note that the
     * time point for wind approximation is taken to be a reference time point selected based on the mark passings
     * for the respective leg's from/to waypoints.
     */
    protected Distance getWindwardDistanceTraveled(Competitor competitor, Waypoint from, TimePoint timePoint) {
        TrackedLegOfCompetitor currentLeg = getTrackedRace().getCurrentLeg(competitor, timePoint);
        final Distance result;
        if (currentLeg == null || from == null) {
            result = null;
        } else {
            Distance d = Distance.NULL;
            boolean count = false; // start counting only once the "from" waypoint has been found
            for (TrackedLeg trackedLeg : getTrackedRace().getTrackedLegs()) {
                count = count || trackedLeg.getLeg().getFrom() == from;
                if (count) {
                    if (trackedLeg.getLeg() == currentLeg.getLeg()) {
                        // partial distance sailed:
                        d = d.add(trackedLeg.getWindwardDistanceFromLegStart(getTrackedRace().getTrack(competitor)
                                .getEstimatedPosition(timePoint, /* extrapolate */true)));
                        break;
                    } else {
                        d = d.add(trackedLeg.getWindwardDistance());
                    }
                }
            }
            result = d;
        }
        return result;
    }
}
