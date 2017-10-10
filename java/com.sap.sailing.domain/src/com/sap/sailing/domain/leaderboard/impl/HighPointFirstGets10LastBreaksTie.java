package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class HighPointFirstGets10LastBreaksTie extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = 1L;

    public HighPointFirstGets10LastBreaksTie() {
        super(10.0);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_FIRST_GETS_TEN;
    }

    @Override
    public int compareByBetterScore(Competitor o1, List<Util.Pair<RaceColumn, Double>> o1Scores, Competitor o2, List<Util.Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter, TimePoint timePoint, Leaderboard leaderboard) {
        return 0;
    }
}
