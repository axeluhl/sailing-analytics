package com.sap.sailing.domain.leaderboard.impl;

import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;


/**
 * A variant of the {@link HighPoint} scoring system where the winner gets a fixed number of points, and
 * all subsequent ranks get one point less. No negative scores are returned. 0.0 is the worst score a
 * competitor can score.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public abstract class HighPointFirstGetsFixedScore extends HighPoint {
    private static final long serialVersionUID = -2767385186133743330L;

    private final double scoreForRaceWinner;
    
    public HighPointFirstGetsFixedScore(double scoreForRaceWinner) {
        this.scoreForRaceWinner = scoreForRaceWinner;
    }

    @Override
    public Double getScoreForRank(RaceColumn raceColumn, Competitor competitor, int rank, Callable<Integer> numberOfCompetitorsInRaceFetcher, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher) {
        Double result;
        if (rank == 0) {
            result = null;
        } else {
            result = Math.max(0.0, (double) (scoreForRaceWinner - rank + 1));
        }
        return result;
    }
}
