package com.sap.sailing.domain.ranking;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Function;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.impl.NoCachingWindLegTypeAndLegBearingCache;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

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
    public interface CompetitorRankingInfo extends Serializable {
        /**
         * For which time point in the race was this ranking information computed?
         */
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
            return getActualTime() == null ? null : getActualTime().plus(
                    getEstimatedActualDurationFromTimePointToCompetitorFarthestAhead());
        }
    
        /**
         * The corrections applied to the time and distance sailed when the {@link #competitor} would have reached the
         * competitor farthest ahead (which would be the case {@link #estimatedActualDurationToCompetitorFarthestAhead} after
         * {@link #timePoint}).
         */
        Duration getCorrectedTimeAtEstimatedArrivalAtCompetitorFarthestAhead();
    }

    public interface RankingInfo extends Serializable {
        /**
         * The time point for which this ranking information is valid
         */
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
    }

    /**
     * @return the tracked race to which this ranking metric is specific
     */
    TrackedRace getTrackedRace();

    default Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint) {
        return getRaceRankingComparator(timePoint, new NoCachingWindLegTypeAndLegBearingCache());
    }

    Comparator<Competitor> getRaceRankingComparator(TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

    default Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint) {
        return getLegRankingComparator(trackedLeg, timePoint, new NoCachingWindLegTypeAndLegBearingCache());
    }

    Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

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
        return getCorrectedTime(competitor, timePoint, new NoCachingWindLegTypeAndLegBearingCache());
    }

    /**
     * Same as {@link #getCorrectedTime(Competitor, TimePoint)}, but allowing the caller to pass a cache that
     * accelerates some calculations.
     */
    Duration getCorrectedTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

    /**
     * Determines in <code>competitor</code>'s own time how much time earlier she would have had to be where she is at
     * {@link RankingInfo#getTimePoint() the ranking info's time point} in order to rank equal to the race leader.
     * 
     * @param rankingInfo
     *            the pre-calculated ranking info for all competitors for a certain time point, as returned by
     *            {@link AbstractRankingMetric#getRankingInfo(TimePoint, WindLegTypeAndLegBearingCache)}
     * @param competitor
     *            the competitor for which to tell the gap to the leader in <code>competitor</code>'s own time
     */
    default Duration getGapToLeaderInOwnTime(RankingMetric.RankingInfo rankingInfo, Competitor competitor) {
        return getGapToLeaderInOwnTime(rankingInfo, competitor, new NoCachingWindLegTypeAndLegBearingCache());
    }
    
    default Duration getGapToLeaderInOwnTime(Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return getGapToLeaderInOwnTime(getRankingInfo(timePoint, cache), competitor, cache);
    }
    
    default Duration getGapToLeaderInOwnTime(Competitor competitor, TimePoint timePoint) {
        return getGapToLeaderInOwnTime(competitor, timePoint, new NoCachingWindLegTypeAndLegBearingCache());
    }
    
    default RankingInfo getRankingInfo(TimePoint timePoint) {
        return getRankingInfo(timePoint, new NoCachingWindLegTypeAndLegBearingCache());
    }
    
    RankingInfo getRankingInfo(TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

    Duration getGapToLeaderInOwnTime(RankingMetric.RankingInfo rankingInfo, Competitor competitor, WindLegTypeAndLegBearingCache cache);
}
