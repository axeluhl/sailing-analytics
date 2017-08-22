package com.sap.sailing.domain.leaderboard.impl;

import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sse.common.TimePoint;


/**
 * The scoring system as used by the Extreme Sailing Series. Disqualified competitors get 0 points; the winner gets as
 * many points as there are competitors in the race. Higher scores are better. No negative scores are returned. Worst
 * score a competitor can get is 1.0.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class HighPoint extends AbstractScoringSchemeImpl {
    private static final long serialVersionUID = -2767385186133743330L;

    public HighPoint() {
        super(/* higherIsBetter */ true);
    }

    @Override
    public Double getScoreForRank(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor,
            int rank,
            Callable<Integer> numberOfCompetitorsInRaceFetcher, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint) {
        Double result;
        if (rank == 0) {
            result = null;
        } else {
            Integer numberOfCompetitorsInRace;
            try {
                numberOfCompetitorsInRace = numberOfCompetitorsInRaceFetcher.call();
                int competitorFleetOrdering;
                if (numberOfCompetitorsInRace == null) {
                    result = null;
                } else {
                    final int effectiveRank;
                    final int numberOfCompetitorsDeterminingMaxPoints;
                    if (raceColumn.hasSplitFleetContiguousScoring()
                            && (competitorFleetOrdering = raceColumn.getFleetOfCompetitor(competitor).getOrdering()) != 0) {
                        int numberOfCompetitorsInBetterFleets = getNumberOfCompetitorsInBetterFleets(raceColumn,
                                competitorFleetOrdering);
                        effectiveRank = rank + numberOfCompetitorsInBetterFleets;
                        numberOfCompetitorsDeterminingMaxPoints = numberOfCompetitorsInLeaderboardFetcher.getNumberOfCompetitorsInLeaderboard();
                    } else {
                        effectiveRank = rank;
                        numberOfCompetitorsDeterminingMaxPoints = numberOfCompetitorsInRace;
                    }
                    result = (double) (numberOfCompetitorsDeterminingMaxPoints - effectiveRank + 1);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @Override
    public Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason, Integer numberOfCompetitorsInRace,
            NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint, Leaderboard leaderboard) {
        return 0.0;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT;
    }

    @Override
    public boolean isValidInNetScore(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor, TimePoint at) {
        return true;
    }
    
}
