package com.sap.sailing.domain.leaderboard.impl;

import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sse.common.TimePoint;

/**
 * Similar to {@link LowPointFirstToWinTwoRaces}, but three races are needed to win. If the regatta has one or more
 * medal series then in those medal series the wins are counted as the primary ordering criterion. If such a medal
 * series starts with a non-discardable carry column then the meaning of that column is changed to mean a number of wins
 * carried into the series. For example, in the 2024 Olympic kite format the winner of the opening series carries two
 * wins, the competitor ranking second at the end of the opening series one win into the grand final which is to be
 * represented as a second medal series following a first semi-final medal series; and boats ranking third and fourth
 * after the opening series carry two wins into the semi-final medal series, and boats ranking fifth and sixth after the
 * opening series carry one win each into the semi-final medal series.
 * <p>
 * 
 * Competitors that score in a later medal series are considered better than those that don't (promotion / elimination
 * scheme).
 * <p>
 * 
 * Those carried wins are added to the
 * {@link #isWin(com.sap.sailing.domain.leaderboard.Leaderboard, com.sap.sailing.domain.base.Competitor, com.sap.sailing.domain.base.RaceColumn, com.sap.sse.common.TimePoint, com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache)
 * wins} achieved in the medal series. More wins rank better. Equal numbers of races won make the score in the last race
 * in that series the first tie-breaker. Note that "last race" can be different races in case of multiple fleets in that
 * medal series, such as in a semi-final medal series split into fleets A and B. Should the tie not be resolved this
 * way, the next and last tie-breaking criterion is the opening series rank where the "opening series" is comprised of
 * all {@link Series} preceding any {@link Series#isMedal() medal series} and may itself consist of more than one
 * {@link Series} object, such as a "Qualification" and a "Final" series in case the fleet is split.
 * <p>
 * 
 * When a medal series is specified to {@link Series#isStartsWithZeroScore() start with zero scores} then this is also
 * applied to the number of wins counted so far. Therefore, if a second medal series follows a first medal series and
 * the second medal series has {@link Series#isStartsWithZeroScore()} set to {@code true} then the wins start counting
 * with zero, then a {@link Series#isFirstColumnNonDiscardableCarryForward() carry column} will be interpreted to hold
 * the number of wins carried into this series, and further wins will be added to that.
 * <p>
 * 
 * This scoring scheme changes the definition of the net points sum for all medal series participants to equal the
 * number of wins, considering the {@link Series#isStartsWithZeroScore()} and the wins carried over into the series as
 * specified by {@link Series#isFirstColumnNonDiscardableCarryForward()}.
 */
public class LowPointFirstToWinThreeRaces extends LowPoint {
    private static final long serialVersionUID = 7072175334160798617L;

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_FIRST_TO_WIN_THREE_RACES;
    }

    /**
     * Still returns {@code null} if {@link Leaderboard#getNetPoints(Competitor, RaceColumn, TimePoint, Set)} returns {@code null}.
     * Otherwise, if the {@code raceColumn} is a medal race column and not the medal series' carry-forward column, 1.0 is returned
     * for a win, 0.0 for non-wins. The carry-column's contents in a medal series are returned as defined by the leaderboard.
     */
    @Override
    public Double getNetPointsForScoreSum(AbstractSimpleLeaderboardImpl leaderboard, Competitor competitor,
            RaceColumn raceColumn, TimePoint timePoint, Set<RaceColumn> discardedRaceColumns) {
        final Double result;
        final Double netPoints = super.getNetPointsForScoreSum(leaderboard, competitor, raceColumn, timePoint, discardedRaceColumns);
        if (netPoints != null && raceColumn.isMedalRace() && !raceColumn.isCarryForward()) {
            result = isWin(leaderboard, competitor, raceColumn, timePoint, new LeaderboardDTOCalculationReuseCache(timePoint)) ? // TODO bug 5778: consider passing through a cache object
                    1.0 : 0.0;
        } else {
            result = netPoints; // includes the null case
        }
        return result;
    }

    @Override
    public boolean isCarryForwardInMedalsCriteria() {
        return false;
    }
    
    @Override
    public int getTargetAmountOfMedalRaceWins() {
        return 3;
    }

    /**
     * In equal-weighted semifinal fleets A/B a different number of races may be sailed until any one competitor
     * in that fleet has reached {@link #getTargetAmountOfMedalRaceWins()} wins. Therefore, the number of races
     * scored is not a criterion for this scoring scheme.
     */
    @Override
    public int compareByNumberOfRacesScored(int competitor1NumberOfRacesScored, int competitor2NumberOfRacesScored) {
        return 0;
    }
    
    @Override
    public double getScoreFactor(RaceColumn raceColumn) {
        Double factor = raceColumn.getExplicitFactor();
        if (factor == null) {
            factor = 1.0;
        }
        return factor;
    }
}
