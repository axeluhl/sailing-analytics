package com.sap.sailing.domain.ranking;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Function;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;

/**
 * Evaluates a {@link Competitor}'s ranking metric, e.g., based on the windward distance to overall leader in case of a
 * one-design class or according to some handicapping scheme given different competitors in the race different time
 * allowances based on distance traveled or time spent. The metric produces a {@link Comparable} value of type
 * <code>T</code> which can be used to order competitors by rank. A {@link Comparator} can be acquired from this metric
 * that handles the calculation and caching of these values for a given race (or for a given leg) and time point so that
 * during sorting a competitor collection the expensive calculations don't need to be carried out redundantly.
 * <p>
 * 
 * Furthermore, this metric knows how to evaluate a {@link Competitor}'s corrected time and how to compute the windward
 * distance to another competitor, both, on a per-leg basis as well as for the entire race.
 * <p>
 * 
 * A handicap-based metric may need to request specific parameters from the {@link Competitor} to judge the ranking
 * correctly, such as the time-on-time multiplier, the Yardstick number, the time-on-distance allowance or the complex
 * ORC Performance Curve rating.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public interface RankingMetric extends Serializable {
    public interface CompetitorRankingInfo extends Timed, Serializable {
        /**
         * For which time point in the race was this ranking information computed?
         */
        @Override
        TimePoint getTimePoint();
    
        /**
         * Whose ranking does this object describe?
         */
        Competitor getCompetitor();
    
        /**
         * How far did {@link #competitor} actually sail windward / along track from the start of the race until
         * {@link #timePoint}? <code>null</code> before the race start; {@link Distance#NULL} after the race start
         * until {@link #competitor} has actually started.
         */
        Distance getWindwardDistanceSailed();
    
        /**
         * Usually the difference between {@link #timePoint} and the start of the race; <code>null</code> if the
         * race start time is not known.
         */
        Duration getActualTime();
    
        /**
         * The corrected time for the {@link #competitor}, assuming the race ended at {@link #timePoint}. This
         * is applying the handicaps proportionately to the time and distance the competitor sailed so far.
         */
        Duration getCorrectedTime();
    
        /**
         * Based on the {@link #competitor}'s average VMG in the current leg and the windward position
         * of the competitor that is farthest ahead in the race, how long would it take {@link #competitor}
         * to reach the competitor farthest ahead if that competitor stopped at {@link #timePoint}?
         */
        Duration getEstimatedActualDurationFromTimePointToCompetitorFarthestAhead();
        
        default Duration getEstimatedActualDurationFromRaceStartToCompetitorFarthestAhead() {
            final Duration estimatedActualDurationFromTimePointToCompetitorFarthestAhead = getEstimatedActualDurationFromTimePointToCompetitorFarthestAhead();
            final Duration actualTime = getActualTime();
            return actualTime == null ? null :
                estimatedActualDurationFromTimePointToCompetitorFarthestAhead == null ? null :
                    actualTime.plus(estimatedActualDurationFromTimePointToCompetitorFarthestAhead);
        }
    
        /**
         * The corrections applied to the time and distance sailed when the {@link #competitor} would have reached the
         * competitor farthest ahead (which would be the case {@link #estimatedActualDurationToCompetitorFarthestAhead} after
         * {@link #timePoint}).
         */
        Duration getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead();
    }

    public interface RankingInfo extends Timed, Serializable {
        /**
         * The time point for which this ranking information is valid
         */
        @Override
        TimePoint getTimePoint();
    
        /**
         * The basic information for each competitor, telling about actual and corrected times as well as information
         * about actual and corrected times needed to reach the position of the competitor farthest ahead at
         * {@link #timePoint}.
         */
        Function<Competitor, CompetitorRankingInfo> getCompetitorRankingInfo();
    
        Competitor getCompetitorFarthestAhead();
    
        /**
         * The competitor with the least corrected time for her arrival at {@link #competitorFarthestAhead}'s windward
         * position at {@link #timePoint}.
         */
        Competitor getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead();

        /**
         * Similar to {@link #getLeaderByCorrectedEstimatedTimeToCompetitorFarthestAhead()}, but relative to a
         * {@link Leg}. This will not consider any progress or position beyond the finishing of that <code>leg</code>.
         * Instead, for those competitors who have already finished the leg, their
         * {@link TrackedLegOfCompetitor#getFinishTime() finishing time} for the leg is used, and the distance traveled
         * is normalized to the {@link TrackedLeg#getWindwardDistance() windward distance of the leg} at the
         * {@link TrackedLeg#getReferenceTimePoint() reference time point}.
         */
        Competitor getLeaderInLegByCalculatedTime(Leg leg, WindLegTypeAndLegBearingCache cache);
        
        Competitor getCompetitorFarthestAheadInLeg(Leg leg, TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

        Duration getActualTimeFromRaceStartToReachFarthestAheadInLeg(Competitor competitor, Leg leg, WindLegTypeAndLegBearingCache cache);
    }
    
    public interface LegRankingInfo extends Timed, Serializable {
        /**
         * The time point for which this ranking information is valid
         */
        @Override
        TimePoint getTimePoint();
    }

    /**
     * @return the tracked race to which this ranking metric is specific
     */
    TrackedRace getTrackedRace();

    default Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint) {
        return getRaceRankingComparator(timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
    }

    Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

    default Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint) {
        return getLegRankingComparator(trackedLeg, timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
    }

    Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

    /**
     * Determine the time sailed for the {@code competitor} at {@code timePoint} in this race. This ignores whether or not
     * the race has recorded a start mark passing for the {@code competitor}. If no finish mark passing is found either, the
     * duration between the {@link #getStartOfRace() race start time} and {@code timePoint} is returned; otherwise the duration
     * between the {@link #getStartOfRace() race start time} and the time when the {@code competitor} finished the race.
     */
    Duration getActualTimeSinceStartOfRace(Competitor competitor, TimePoint timePoint);

    /**
     * How much time did the <code>competitor</code> spend in the {@link TrackedRace#getRace() race} described by
     * <code>trackedRace</code> at <code>timePoint</code>, corrected by the handicapping rules defined by this ranking
     * metric? If the competitor hasn't started the race yet at <code>timePoint</code>, <code>null</code> is returned.
     * If the competitor has finished the race already at <code>timePoint</code>, the time it took the competitor to
     * complete the race, corrected by any handicap, is returned. If the competitor didn't finish the leg before the end
     * of tracking, <code>null</code> is returned because this indicates that the tracker stopped sending valid data.
     * <p>
     * 
     * The result will also be <code>null</code> if the course does not have any waypoints or the race hasn't started
     * yet at <code>timePoint</code>.
     * <p>
     * 
     * This method can also be used to calculate the corrected times at the end of each leg, simply by passing the time
     * when the competitor finished the respective leg as <code>timePoint</code>.
     */
    default Duration getCorrectedTime(Competitor competitor, TimePoint timePoint) {
        return getCorrectedTime(competitor, timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
    }

    /**
     * Same as {@link #getCorrectedTime(Competitor, TimePoint)}, but allowing the caller to pass a cache that
     * accelerates some calculations.
     */
    Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

    /**
     * Determines in <code>competitor</code>'s own time how much time earlier she would have had to be where she is at
     * {@link RankingInfo#getTimePoint() the ranking info's time point} in order to rank equal to the race leader. Note
     * that this metric is not necessarily restricted to the scope of a single leg. The <code>competitor</code> will be
     * projected to the position of the fastest boat which may be well beyond the <code>competitor</code>'s current
     * leg end.
     * 
     * @param rankingInfo
     *            the pre-calculated ranking info for all competitors for a certain time point, as returned by
     *            {@link AbstractRankingMetric#getRankingInfo(TimePoint, WindLegTypeAndLegBearingCache)}
     * @param competitor
     *            the competitor for which to tell the gap to the leader in <code>competitor</code>'s own time
     *            
     * @see #getLegGapToLegLeaderInOwnTime(TrackedLegOfCompetitor, TimePoint, com.sap.sailing.domain.ranking.RankingMetric.CompetitorRankingInfo.RankingInfo, WindLegTypeAndLegBearingCache)
     */
    default Duration getGapToLeaderInOwnTime(RankingInfo rankingInfo, Competitor competitor) {
        return getGapToLeaderInOwnTime(rankingInfo, competitor, new LeaderboardDTOCalculationReuseCache(rankingInfo.getTimePoint()));
    }
    
    default Duration getGapToLeaderInOwnTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return getGapToLeaderInOwnTime(getRankingInfo(timePoint, cache), competitor, cache);
    }
    
    default Duration getGapToLeaderInOwnTime(Competitor competitor, TimePoint timePoint) {
        return getGapToLeaderInOwnTime(competitor, timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
    }
    
    default RankingInfo getRankingInfo(TimePoint timePoint) {
        return getRankingInfo(timePoint, new LeaderboardDTOCalculationReuseCache(timePoint));
    }
    
    RankingInfo getRankingInfo(TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

    Duration getGapToLeaderInOwnTime(RankingInfo rankingInfo, Competitor competitor, WindLegTypeAndLegBearingCache cache);

    /**
     * Computes the <code>competitor</code>'s gap in own time to the leader (or best leg finisher, in corrected time if
     * handicap ranking is in effect) of the leg that the <code>competitor</code> is in at <code>timePoint</code> (or
     * the last leg if <code>timePoint</code> is after <code>competitor</code> has finished the race). Returns
     * <code>null</code> if <code>timePoint</code> is before <code>competitor</code> has started.
     * 
     * @param rankingInfo
     *            tells about leader (by calculated time) and boat farthest ahead
     */
    Duration getLegGapToLegLeaderInOwnTime(TrackedLegOfCompetitor trackedLegOfCompetitor, TimePoint timePoint,
            RankingInfo rankingInfo, WindLegTypeAndLegBearingCache cache);
}
