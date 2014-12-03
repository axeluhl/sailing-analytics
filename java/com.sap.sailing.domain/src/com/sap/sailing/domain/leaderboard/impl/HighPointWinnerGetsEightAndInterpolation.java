package com.sap.sailing.domain.leaderboard.impl;

import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;

/**
 * High point system with a fixed 'highest score' (eight points) and
 * an adaptation of the scoring when a fleet has less competitors.
 * Normally the winner gets 8 points, second gets 7 points and so on.
 * If a fleet has less competitors than 8 the scores are interpolated between the maximum and the minimum score.
 * Used by the champions league event 2014
 * @author Frank
 *
 */
public class HighPointWinnerGetsEightAndInterpolation extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = 4845740206581229807L;

    private static int MAX_NUMBER_OF_COMPETITORS_PER_FLEET = 8;
    
    public HighPointWinnerGetsEightAndInterpolation() {
        super(/* scoreForRaceWinner */ MAX_NUMBER_OF_COMPETITORS_PER_FLEET);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_WINNER_GETS_EIGHT_AND_INTERPOLATION;
    }
    
    @Override
    public Double getScoreForRank(RaceColumn raceColumn, Competitor competitor, int rank, Callable<Integer> numberOfCompetitorsInRaceFetcher, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher) {
        final int effectiveRank = getEffectiveRank(raceColumn, competitor, rank);
        final Double result;
        if (effectiveRank == 0) {
            result = null;
        } else {
            try {
                Integer numberOfCompetitorsInRace = numberOfCompetitorsInRaceFetcher.call();
                if(numberOfCompetitorsInRace <= MAX_NUMBER_OF_COMPETITORS_PER_FLEET) {
                    // Formula = 8- ((Max-Min) / (Max-Min - numberOfMissingBoats) * (fleetRank-1))
                    double minMaxRange = getScoreForRaceWinner() - getMinimumScoreFromRank();
                    int numberOfMissingBoatsInFleet = MAX_NUMBER_OF_COMPETITORS_PER_FLEET - numberOfCompetitorsInRace;
                    double score = getScoreForRaceWinner() - (minMaxRange / (minMaxRange - numberOfMissingBoatsInFleet) * (effectiveRank-1));
                    result = Math.max(getMinimumScoreFromRank(), score);
                } else {
                    result = null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }}
