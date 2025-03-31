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
 * Turns off the A8.1 comparison for opening series tie-breaking, thus defaulting to A8.2 (last race,
 * then second-to-last, etc.).
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LowPointFirstToWinThreeRacesA82Only extends LowPointFirstToWinThreeRaces {
    private static final long serialVersionUID = 8820858236331467431L;

    @Override
    protected int compareByA81TieBreak(Competitor o1, List<Pair<RaceColumn, Double>> o1Scores, Competitor o2,
            List<Pair<RaceColumn, Double>> o2Scores, Iterable<RaceColumn> raceColumnsToConsider, boolean nullScoresAreBetter,
            TimePoint timePoint, Leaderboard leaderboard,
            Map<Competitor, Set<RaceColumn>> discardedRaceColumnsPerCompetitor,
            BiFunction<Competitor, RaceColumn, Double> totalPointsSupplier, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        return 0;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_FIRST_TO_WIN_THREE_RACES_A82_ONLY;
    }
}
