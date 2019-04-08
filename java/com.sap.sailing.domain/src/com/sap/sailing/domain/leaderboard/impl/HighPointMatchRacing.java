package com.sap.sailing.domain.leaderboard.impl;

import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sse.common.TimePoint;

/**
 * Scoring system indented to by used for match racing where the winner gets 1 points and the loser gets 0 points
 */
public class HighPointMatchRacing extends HighPoint {
    private static final long serialVersionUID = 9124974081268072297L;

    public HighPointMatchRacing() {
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_MATCH_RACING;
    }

    @Override
    public Double getScoreForRank(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor, int rank,
            Callable<Integer> numberOfCompetitorsInRaceFetcher, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint) {
        return rank == 0 ? null : rank == 1 ? 1.0 : 0.0;
    }

}
