package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * Eliminates the A8.1 comparison (sort scores by better/worst and compare one by one) and hence
 * defaults to A8.2 (last race, second-to-last race, etc.).
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LowPointA82Only extends LowPoint {
    private static final long serialVersionUID = 4715596377202890920L;

    @Override
    public int compareByBetterScore(Competitor o1, List<Pair<RaceColumn, Double>> o1Scores, Competitor o2,
            List<Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter, TimePoint timePoint,
            Leaderboard leaderboard, Map<Competitor, Set<RaceColumn>> discardedRaceColumnsPerCompetitor,
            BiFunction<Competitor, RaceColumn, Double> totalPointsSupplier,
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return 0;
    }
    
    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_A82_ONLY;
    }
}
