package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class HighPointFirstGets1LastBreaksTie extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = 1L;

    public HighPointFirstGets1LastBreaksTie() {
        super(10.0);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_FIRST_GETS_ONE;
    }

    @Override
    public int compareByBetterScore(List<Pair<RaceColumn, Double>> o1Scores, List<Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter) {
        return 0;
    }
}
