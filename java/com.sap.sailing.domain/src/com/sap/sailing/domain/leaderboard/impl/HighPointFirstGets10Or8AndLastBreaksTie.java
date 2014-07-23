package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.analyzing.impl.AdditionalScoringInformationFinder;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationType;
import com.sap.sse.common.Util;

/**
 * {@link HighPointFirstGetsFixedScore} scheme that in most cases applies
 * 10 points for the winner. Iff for one race column a {@link AdditionalScoringInformationEvent}
 * is found then 8 points are applied for the winner.
 * 
 * @author Simon Marcel Pamies
 */
public class HighPointFirstGets10Or8AndLastBreaksTie extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = 1L;
    
    private final double SCORE_FOR_WINNER_IF_OVERWRITTEN = 8.0;

    public HighPointFirstGets10Or8AndLastBreaksTie() {
        super(10.0);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_FIRST_GETS_TEN_OR_EIGHT;
    }

    @Override
    public int compareByBetterScore(List<Util.Pair<RaceColumn, Double>> o1Scores, List<Util.Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter) {
        return 0;
    }
    
    @Override
    public Double getScoreForRank(RaceColumn raceColumn, Competitor competitor, int rank, Callable<Integer> numberOfCompetitorsInRaceFetcher, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher) {
        Double effectiveScore = super.getScoreForRank(raceColumn, competitor, rank, numberOfCompetitorsInRaceFetcher, numberOfCompetitorsInLeaderboardFetcher);
        return checkForOverwrittenScore(raceColumn, rank, effectiveScore);
    }
    
    private Double checkForOverwrittenScore(RaceColumn raceColumn, int rank, Double effectiveScore) {
        Double result = effectiveScore;
        for (Fleet fleet : raceColumn.getFleets()) {
            RaceLog raceLog = raceColumn.getRaceLog(fleet);
            AdditionalScoringInformationFinder finder = new AdditionalScoringInformationFinder(raceLog);
            List<AdditionalScoringInformationEvent> events = finder.analyze();
            for (AdditionalScoringInformationEvent event : events) {
                if (event != null && event.getType() == AdditionalScoringInformationType.MAX_POINTS_DIMINISH_MAX_SCORE) {
                    if (rank == 0) {
                        result = null;
                    } else {
                        result = Math.max(getMinimumScoreFromRank(), (double) (SCORE_FOR_WINNER_IF_OVERWRITTEN - rank + 1));
                        
                        // the list returned from finder has newest first - if we find an event
                        // that matches the type that we want then we can stop as it is the newest
                        // unrevoked that has been stored
                        break;
                    }
                }
            }
        }
        return result;
    }
}
