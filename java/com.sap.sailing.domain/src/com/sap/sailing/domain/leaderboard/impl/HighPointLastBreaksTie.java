package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sse.common.UtilNew;

/**
 * In this variant of the {@link HighPoint} scoring scheme, scoring by "better race" (sometimes referred to as a
 * "back run" where the races are sorted with best races first and the first difference is counted) is disabled, such
 * that even if the last race is not a medeal race, the last race score decides the ordering.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class HighPointLastBreaksTie extends HighPoint {
    private static final long serialVersionUID = -5338636946886101669L;

    @Override
    public int compareByBetterScore(List<UtilNew.Pair<RaceColumn, Double>> o1Scores, List<UtilNew.Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter) {
        return 0;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_LAST_BREAKS_TIE;
    }
}
