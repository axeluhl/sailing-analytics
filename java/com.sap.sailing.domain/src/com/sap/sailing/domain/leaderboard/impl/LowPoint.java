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
 * The scoring system as used by the ISAF standard scoring scheme, also known as the "Low Point Scoring System."
 * Scores are primarily attributed according to rank, so a race's winner gets score 1.00 and so on. Lower scores are
 * therefore better.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LowPoint extends AbstractScoringSchemeImpl {
    private static final long serialVersionUID = -2767385186133743330L;

    public LowPoint() {
        super(/* higherIsBetter */ false);
    }

    @Override
    public Double getScoreForRank(RaceColumn raceColumn, Competitor competitor, int rank, Callable<Integer> numberOfCompetitorsInRaceFetcher, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher) {
        final int effectiveRank;
        final Double result;
        int competitorFleetOrdering;
        if (rank == 0) {
            effectiveRank = 0;
        } else if (raceColumn.hasSplitFleetContiguousScoring() && (competitorFleetOrdering=raceColumn.getFleetOfCompetitor(competitor).getOrdering()) != 0) {
            int numberOfCompetitorsInBetterFleets = getNumberOfCompetitorsInBetterFleets(raceColumn, competitorFleetOrdering);
            effectiveRank = rank + numberOfCompetitorsInBetterFleets;
        } else {
            effectiveRank = rank;
        }
        result = effectiveRank == 0 ? null : (double) effectiveRank;
        return result;
    }

    @Override
    public Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason,
            Integer numberOfCompetitorsInRace, NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher) {
        Double result;
        if (numberOfCompetitorsInRace == null || raceColumn.hasSplitFleetContiguousScoring()) {
            result = (double) (numberOfCompetitorsInLeaderboardFetcher.getNumberOfCompetitorsInLeaderboard()+1);
        } else {
            result = (double) (numberOfCompetitorsInRace+1);
        }
        return result;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT;
    }

    @Override
    public boolean isValidInTotalScore(Leaderboard leaderboard, RaceColumn raceColumn, TimePoint at) {
        return true;
    }

    /**
     * For low-point scoring schemes, we don't want to see boats at the top of the leaderboard just because they have
     * not scored in any races yet. Although generally we don't want to consider the number of races, this implementation
     * makes an exception for the case that no races were sailed by a competitor. No races is worse than one or more races.
     * Other than that, this implementation ignores the number of races.
     */
    @Override
    public int compareByNumberOfRacesScored(int competitor1NumberOfRacesScored, int competitor2NumberOfRacesScored) {
        return (int) Math.signum(competitor1NumberOfRacesScored) - (int) Math.signum(competitor2NumberOfRacesScored);
    }

}
