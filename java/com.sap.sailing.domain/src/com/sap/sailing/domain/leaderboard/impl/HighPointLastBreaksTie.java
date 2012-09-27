package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;

import com.sap.sailing.domain.common.ScoringSchemeType;

/**
 * In this variant of the {@link HighPoint} 
 * @author Axel Uhl (d043530)
 *
 */
public class HighPointLastBreaksTie extends HighPoint {
    private static final long serialVersionUID = -5338636946886101669L;

    @Override
    public int compareByBetterScore(List<Double> o1Scores, List<Double> o2Scores, boolean nullScoresAreBetter) {
        return 0;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_LAST_BREAKS_TIE;
    }
}
