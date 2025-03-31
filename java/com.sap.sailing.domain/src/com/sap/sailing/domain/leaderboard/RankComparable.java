package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.ranking.RankingMetric;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * RankComparable makes it possible to compare {@link Competitor}s across fleets and therefore regardless of their {@link TrackedRace}. 
 * Though it is assumed that the {@link TrackedRace} of the Competitors have the same {@link RankingMetric} because the type of comparison 
 * (time-based, distance-based or other metrics) depends on them.
 */
public interface RankComparable extends Comparable<RankComparable> {
    
    @Override
    int compareTo(RankComparable otherRankComparable); 
    
}