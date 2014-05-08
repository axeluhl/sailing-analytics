package com.sap.sailing.domain.leaderboard.impl;

import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;

public class HighPointWinnerGetsSixLastGetsOneOthersAreInterpolated extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = -4116295702464748719L;

    public HighPointWinnerGetsSixLastGetsOneOthersAreInterpolated() {
        super(/* scoreForRaceWinner */ 6);
    }

    @Override
    public Double getScoreForRank(RaceColumn raceColumn, Competitor competitor, int rank,
            Callable<Integer> numberOfCompetitorsInRaceFetcher,
            NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher) {
        // TODO: find out what the rules for interpolation shall be and implement accordingly
        return super.getScoreForRank(raceColumn, competitor, rank, numberOfCompetitorsInRaceFetcher,
                numberOfCompetitorsInLeaderboardFetcher);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_WINNER_GETS_SIX_LAST_GETS_ONE_OTHERS_ARE_INTERPOLATED;
    }
}
