package com.sap.sailing.domain.leaderboard.impl;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

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
    public Double getScoreForRank(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor, int rank,
            Callable<Integer> numberOfCompetitorsInRaceFetcher,
            NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint) {
        final Double result;
        final int effectiveRank = getEffectiveRank(raceColumn, competitor, rank);
        result = effectiveRank == 0 ? null : (double) effectiveRank;
        return result;
    }

    /**
     * Considers contiguous scoring for split-fleet columns, finding out in which fleet the <code>competitor</code> races
     * in the <code>raceColumn</code> and figuring out how many competitors race in better fleets. For non-contiguous
     * scoring, the effective rank equals the <code>rank</code> in the competitor's fleet.
     */
    protected int getEffectiveRank(RaceColumn raceColumn, Competitor competitor, int rank) {
        final int competitorFleetOrdering;
        final int effectiveRank;
        if (rank == 0) {
            effectiveRank = 0;
        } else if (raceColumn.hasSplitFleetContiguousScoring() && (competitorFleetOrdering=raceColumn.getFleetOfCompetitor(competitor).getOrdering()) != 0) {
            int numberOfCompetitorsInBetterFleets = getNumberOfCompetitorsInBetterFleets(raceColumn, competitorFleetOrdering);
            effectiveRank = rank + numberOfCompetitorsInBetterFleets;
        } else {
            effectiveRank = rank;
        }
        return effectiveRank;
    }

    /**
     * For an {@link MaxPointsReason#STP} penalty, the uncorrected score is obtained from the
     * {@code uncorrectedScoreProvider} and incremented by 1.0; otherwise, the number of competitors plus one is used.
     */
    @Override
    public Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason,
            Integer numberOfCompetitorsInRace,
            NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint,
            Leaderboard leaderboard, Supplier<Double> uncorrectedScoreProvider) {
        Double result;
        if (maxPointsReason == MaxPointsReason.STP) {
            final Double uncorrectedScore = uncorrectedScoreProvider.get();
            result = uncorrectedScore == null ? null : uncorrectedScore + 1.0;
        } else if (numberOfCompetitorsInRace == null || raceColumn.hasSplitFleetContiguousScoring()) {
            result = (double) (numberOfCompetitorsInLeaderboardFetcher.getNumberOfCompetitorsInLeaderboard() + 1);
        } else {
            result = (double) (numberOfCompetitorsInRace + 1);
        }
        return result;
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT;
    }

    @Override
    public boolean isValidInNetScore(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor, TimePoint at) {
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
        return (int) Math.signum(competitor2NumberOfRacesScored) - (int) Math.signum(competitor1NumberOfRacesScored);
    }

}
