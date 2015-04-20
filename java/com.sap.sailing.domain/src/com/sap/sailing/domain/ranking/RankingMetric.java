package com.sap.sailing.domain.ranking;

import java.util.Comparator;

import com.sap.sailing.domain.base.Competitor;
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
public interface RankingMetric<T extends Comparable<T>> {
    default Comparator<Competitor> getRankingComparator(TrackedRace trackedRace, TimePoint timePoint) {
        return getRaceRankingComparator(trackedRace, null, new NoCachingWindLegTypeAndLegBearingCache());
    }

    Comparator<Competitor> getRaceRankingComparator(TrackedRace trackedRace, TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

    Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg, TimePoint timePoint, WindLegTypeAndLegBearingCache cache);

    /**
     * Calculates the time that the <code>trailing</code> competitor needs to "catch up" to reach the <code>leading</code> competitor,
     * measured in the <code>trailing</code> competitor's "time system." For one-design ranking this will be the gap based on the
     * <code>trailing</code> competitor's windward/along-course distance to the <code>leading</code> competitor divided by the
     * <code>trailing</code> competitor's current VMG/VMC. For other ranking systems such as time-on-time, time-on-distance,
     * combinations thereof or ORC performance curve scoring special rules will apply which take into account the distance traveled
     * by the <code>trailing</code> competitor in her current leg and scaling any allowances according to that distance in order
     * to compare to the leader. If the leader is already in another leg, the best corrected leg completion time is used as the
     * reference for comparison. This may not be the overall leader of the race at that time, but anything else would require an
     * uncertain prediction of the <code>trailing</code> competitor's performance in the next leg which is not considered to be
     * useful here.
     */
    Duration getTimeToImprove(TrackedRace trackedRace, Competitor trailing, Competitor leading, TimePoint timePoint,
            WindLegTypeAndLegBearingCache cache);

    // TODO provide comparators per race / leg
    // TODO add methods for corrected time and windward distance calculation
}
