package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AdditionalScoringInformationFinder;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sse.common.Util;

/**
 * {@link HighPointFirstGetsFixedScore} scheme that in most cases applies
 * 10 points for the winner. Iff for one race column a {@link RaceLogAdditionalScoringInformationEvent}
 * is found then 8 points are applied for the winner.
 * 
 * From Phil, Race Director, on 12.09.2014: "If there is a tie in the regatta score between two or more boats at any time, the tie
 * shall be broken in favour of the boat that has won the most races. If a tie still remains, it shall be broken in
 * favour of the boat that had the better place at the last race sailed."
 * 
 * @author Simon Marcel Pamies
 */
public class HighPointFirstGets10Or8AndLastBreaksTie extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = 1L;
    
    private final double SCORE_FOR_WINNER_IF_OVERWRITTEN = 8.0;
    private static final int MIN_RACES_REQUIRED_TO_BE_SCORED = 3;

    public HighPointFirstGets10Or8AndLastBreaksTie() {
        super(10.0);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_FIRST_GETS_TEN_OR_EIGHT;
    }

    @Override
    public int compareByBetterScore(List<Util.Pair<RaceColumn, Double>> o1Scores, List<Util.Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter) {
        assert o1Scores.size() == o2Scores.size();
        int o1Wins = getWins(o1Scores);
        int o2Wins = getWins(o2Scores);
        int result = o2Wins - o1Wins;
        return result;
    }

    /**
     * Counts a competitor's wins by comparing the scores to {@link #MAX_POINTS} which is the score attributed to a race
     * won
     */
    private int getWins(List<com.sap.sse.common.Util.Pair<RaceColumn, Double>> scores) {
        int wins = 0;
        for (com.sap.sse.common.Util.Pair<RaceColumn, Double> score : scores) {
            if (Math.abs(score.getB() - getScoreForRaceWinner() * score.getA().getFactor()) < 0.0000001) {
                wins++;
            }
        }
        return wins;
    }
    
    /**
     * Generally, in the Extreme Sailing Series the number of races scored doesn't matter. However, if a competitor
     * scored fewer than five races, the competitor doesn't participate in the overall ranking and is therefore to be
     * sorted worse than all competitors completing five or more races. 
     */
    @Override
    public int compareByNumberOfRacesScored(int competitor1NumberOfRacesScored, int competitor2NumberOfRacesScored) {
        int result;
        if (competitor1NumberOfRacesScored >= MIN_RACES_REQUIRED_TO_BE_SCORED) {
            if (competitor2NumberOfRacesScored >= MIN_RACES_REQUIRED_TO_BE_SCORED) {
                result = 0;
            } else {
                result = -1; // competitor1 is better ("less") because it's the only one of the two scoring more than MIN_RACES_REQUIRED_TO_BE_SCORED
            }
        } else {
            if (competitor2NumberOfRacesScored >= MIN_RACES_REQUIRED_TO_BE_SCORED) {
                result = 1; // competitor2 is better ("less") because it's the only one of the two scoring more than MIN_RACES_REQUIRED_TO_BE_SCORED
            } else {
                result = 0;
            }
        }
        return result;
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
            RaceLogAdditionalScoringInformationEvent event = finder.analyze(AdditionalScoringInformationType.MAX_POINTS_DECREASE_MAX_SCORE);
            if (event != null) {
                if (rank == 0) {
                    result = null;
                } else {
                    result = Math.max(getMinimumScoreFromRank(), (double) (SCORE_FOR_WINNER_IF_OVERWRITTEN - rank + 1));
                }
            }
        }
        return result;
    }
}
