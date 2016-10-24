package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class HighPointFirstGets1LastBreaksTie extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = 1L;

    public HighPointFirstGets1LastBreaksTie() {
        super(1.0);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_FIRST_GETS_ONE;
    }

    @Override
    public Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason, Integer numberOfCompetitorsInRace,
            NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher) {
        return -1.0;
    }

    @Override
    public int compareByBetterScore(Competitor o1, List<Util.Pair<RaceColumn, Double>> o1Scores, Competitor o2, List<Util.Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter, TimePoint timePoint) {
        return 0;
    }
}
