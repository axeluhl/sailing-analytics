package com.sap.sailing.domain.ranking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

/**
 * Evaluates a {@link Competitor}'s ranking metric, either by windward distance to overall leader in case of a
 * one-design class or according to some handicapping scheme. The metric produces a {@link Comparable} value of type
 * <code>T</code> which can be used to order competitors by rank.
 * <p>
 * 
 * Furthermore, this metric knows how to evaluate a {@link Competitor}'s corrected time and how to compute the windward
 * distance to another competitor.
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
    T getRankingMetric(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint);
}
