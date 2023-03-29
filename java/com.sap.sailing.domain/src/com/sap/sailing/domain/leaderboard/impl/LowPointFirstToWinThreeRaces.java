package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

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
 * {@link #isWin(Leaderboard, Competitor, RaceColumn, TimePoint, Function, WindLegTypeAndLegBearingAndORCPerformanceCurveCache)
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

    @Override
    public boolean isMedalWinAmountCriteria() {
        return true;
    }
    
    @Override
    public boolean isLastMedalRaceCriteria() {
        return true;
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
            // TODO bug 5778: consider passing through a cache object
            final LeaderboardDTOCalculationReuseCache cache = new LeaderboardDTOCalculationReuseCache(timePoint);
            result = isWin(leaderboard, competitor, raceColumn, timePoint, c->leaderboard.getTotalPoints(competitor, raceColumn, timePoint, cache),
                    cache) ? 1.0 : 0.0;
        } else {
            result = netPoints; // includes the null case
        }
        return result;
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

    /**
     * A carry-forward column in a medal series for this scoring scheme means that the points in the column represent a
     * number of wins carried forward into this series. Other than that, the regular logic applies: one point for a
     * {@link #isWin(Leaderboard, Competitor, RaceColumn, TimePoint, WindLegTypeAndLegBearingAndORCPerformanceCurveCache, Function<Competitor, Double>)
     * win}, zero for non-win, adding to {@code numberOfMedalRacesWonSoFar} or starting with zero if
     * {@link RaceColumn#isStartsWithZeroScore()}.
     */
    @Override
    public int getWinCount(Leaderboard leaderboard, Competitor competitor, RaceColumn raceColumn,
            final Double totalPoints, TimePoint timePoint, Function<Competitor, Double> totalPointsSupplier, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Integer winCount;
        if (raceColumn.isCarryForward()) {
            winCount = totalPoints == null ? 0 : totalPoints.intValue();
        } else {
            winCount = super.getWinCount(leaderboard, competitor, raceColumn, totalPoints, timePoint, totalPointsSupplier, cache);
        }
        return winCount;
    }

    /**
     * When the competitors have valid medal race scores, this scoring scheme ignores the score sums altogether and
     * assumes that a {@link #compareByMedalRacesWon(int, int) comparison by the number of medal races won} is being
     * performed because {@link #isMedalWinAmountCriteria()} returns {@code true} for this scoring scheme.
     */
    @Override
    public int compareByScoreSum(double o1ScoreSum, double o2ScoreSum, boolean nullScoresAreBetter,
            boolean haveValidMedalRaceScores) {
        return haveValidMedalRaceScores ? 0 : super.compareByScoreSum(o1ScoreSum, o2ScoreSum, nullScoresAreBetter, haveValidMedalRaceScores);
    }

    /**
     * The sum of medal race scores is not of interest to ranking when using this scoring scheme. See
     * {@link #compareByMedalRacesWon(int, int)} instead.
     */
    @Override
    public int compareByMedalRaceScore(Double o1MedalRaceScore, Double o2MedalRaceScore, boolean nullScoresAreBetter) {
        return 0;
    }

    /**
     * The default implementation will do an RRS A8.1 comparison (sort results, compare one by one "from the top" and
     * decide upon the first difference), across <em>all</em> scores throughout the leaderboard. Here, however, we need
     * to distinguish between competitors who only sailed in the opening series (anything preceding the first medal
     * series), and those who did score in at least one medal race, because the medal series are evaluated only by wins
     * and as a secondary tie breaker then by the last medal race score and after that the rank at the end of the opening
     * series.<p>
     * 
     * It is safe to assume that if {@code o1} has valid medal series scores then so will {@code o2}, and vice versa,
     * because otherwise ranking by medal series participation would already have decided who ranks better. If none has
     * score in a medal race then we default to the {@code super} implementation with a default RRS A8.1 decision.<p>
     * 
     * Otherwise (both have scored in at least one medal race) we have to assume that both sailed in the same medal series,
     * scored the same number of wins (including wins carried forward) and were also tied on the scores in their respective
     * last medal race. Then, the decision is to be made based on the opening series rank, which in itself includes the
     * entire tie-breaking rule set with {@link #compareByBetterScore(Competitor, List, Competitor, List, boolean, TimePoint, Leaderboard, Map, BiFunction, WindLegTypeAndLegBearingAndORCPerformanceCurveCache)}
     * etc., only up to the end of the opening series.
     */
    @Override
    public int compareByBetterScore(Competitor o1, List<Pair<RaceColumn, Double>> o1Scores, Competitor o2,
            List<Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter, TimePoint timePoint,
            Leaderboard leaderboard, Map<Competitor, Set<RaceColumn>> discardedRaceColumnsPerCompetitor,
            BiFunction<Competitor, RaceColumn, Double> totalPointsSupplier, WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final int result;
        if (!hasMedalScores(o1Scores)) {
            result = super.compareByBetterScore(o1, o1Scores, o2, o2Scores, nullScoresAreBetter, timePoint, leaderboard,
                    discardedRaceColumnsPerCompetitor, totalPointsSupplier, cache);
        } else {
            final Iterable<RaceColumn> openingSeriesRaceColumns = getOpeningSeriesRaceColumns(leaderboard);
            // pass on the totalPointsSupplier coming from the caller, most likely a LeaderboardTotalRankComparator,
            // to speed up / save the total points (re-)calculation
            result = new LeaderboardTotalRankComparator(leaderboard, timePoint, this, nullScoresAreBetter, openingSeriesRaceColumns, totalPointsSupplier, cache)
                    .compare(o1, o2);
        }
        return result;
    }

    /**
     * Merge non-medal series columns, preserving order across {@code raceColumnsO1} and {@code raceColumnsO2}.
     */
    private Iterable<RaceColumn> getOpeningSeriesRaceColumns(Leaderboard leaderboard) {
        return Util.filter(leaderboard.getRaceColumns(), rc->!rc.isMedalRace());
    }

    private boolean hasMedalScores(List<Pair<RaceColumn, Double>> o1Scores) {
        return o1Scores.stream().anyMatch(p->p.getA().isMedalRace() && p.getB() != null);
    }
}
