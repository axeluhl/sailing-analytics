package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.ranking.RankingMetric;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * RankComparable makes it possible to compare {@link Competitor}s across fleets and therefore regardless of their {@link TrackedRace}. 
 * The type of comparison (time-based, distance-based or other metrics) depends on the {@link RankingMetric} used.
 */
public interface RankComparable extends Comparable<RankComparable> {
}