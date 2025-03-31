package com.sap.sailing.domain.leaderboard.impl;

import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sse.common.TimePoint;

/**
 * As compared to the regular low-point scheme, the 2013 ISAF World Cup will score the winner of a race with
 * 0 points, all others with their ranks. Ties are broken like this:
 * <ul>
 * <li>25.9    RRS B8 is deleted. RRS A8 is changed as follows: 
 * <li> 25.9.1    For boats competing in a medal stage, ties in the regatta score are broken by the medal stage score. 
 * <li> 25.9.2    Ties in a medal stage with a single race are broken by applying RRS A8 to the opening series scores. 
 * <li> 25.9.3    All other series ties shall be broken in accordance with A8.2. 
 * </ul>
 * 
 * Discards are to be constrained to each series, with no discards in the medal series. However, since there is
 * a new rule for carrying the qualification results to the final series, this won't be a regular regatta
 * leaderboard anyhow. Instead, the final series starts out with an artificial race that has its scores based
 * on the competitor rankings after the qualification series, where each fleet's scores again start with 0, then 2, ...
 * So the best boat in the silver fleet will get 0 points in the carry race, the next boat 2, and so on.
 * The carry race is discardable.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LowPointWinnerGetsZero extends LowPoint {
    private static final long serialVersionUID = 3405249102284162690L;

    @Override
    public Double getScoreForRank(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor, int rank, Callable<Integer> numberOfCompetitorsInRacefetcher, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint) {
        return rank == 0 ? null : rank == 1 ? 0.0 : (double) rank;
    }

    // TODO handle tie-breaking rules properly
    
    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_WINNER_GETS_ZERO;
    }
}
