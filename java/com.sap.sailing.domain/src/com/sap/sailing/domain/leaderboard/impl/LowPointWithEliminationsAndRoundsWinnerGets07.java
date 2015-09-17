package com.sap.sailing.domain.leaderboard.impl;

import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * Implements an elimination scheme as used by surfing championships, based on a general low point scoring scheme. A
 * regatta is divided into "eliminations" where each elimination consists of a sequence of "rounds." Each round is in
 * turn divided into a number of "heats" (races). The final round in an elimination consists of a "final" race and a
 * "losers final" race. They constitute, in our terminology, one series with contiguous scoring. The winner of the
 * "losers final" gets one point more than the competitor ranking last in the "final" race. The winner of the final
 * race obtains 0.7 points. All other ranks are assigned points equal to the rank or the average of the ranks attained
 * by competitors ranking equal.
 * <p>
 * 
 * The semi-final has two heats, and the better half of the competitors in those heats get promoted to the "final," the
 * competitors ranking worse will participate in the "losers final." Points are usually not awarded in the semi-final
 * (exceptions for races not sailed see below).
 * <p>
 * 
 * In the quarter-final there are four heats. The competitors ranking in the top half of their heat are promoted to the
 * semi-final and are not awarded points for the quarter-final round (exceptions for races not sailed see below). Those
 * ranking in the bottom half of their heat are not promoted to the next round and are instead awarded points for the
 * elimination based on their rank in their heat. Across all quarter-final heats, competitors not promoted and having
 * the same rank obtain equal points regardless their heat. The number of points is calculated as the average of their
 * rank in the elimination. Quarter-final participants rank better than those eliminated in earlier rounds. Eliminated
 * competitors rank better in the elimination if they obtained a better rank in their heat than other competitors
 * eliminated in the same round. For example, if each of the four quarter-final heat has eight competitors, the
 * four competitors ranking last (8th) in their heat get the average points for ranks 29-32 (30.5 points), whereas
 * the four competitors ranking 7th in their heat get the average points for ranks 25-28 (26.5 points), and so on.
 * <p>
 * 
 * Other rounds preceding the quarter-final work by the same principle as the quarter-final.
 * <p>
 * 
 * Should a planned heat not be sailed then all competitors assigned to that heat will obtain equal points based on the
 * average of the ranks for which they would have sailed. For example, if the final heat cannot be sailed, all competitors
 * who qualified for the final race obtain the average of 0.7, 2, 3, 4, 5, 6, 7 and 8 points (4.46 points).<p>
 * 
 * The regatta is expected to be modeled such that fleet ordering numbers are used in a special way. Ordering <code>1</code>
 * is to be used to identify the Final heat, <code>2</code> for the Losers Final heat. For each earlier round the
 * ordering is increased by one and applied equally to all heats of that round. Therefore, the two semi-final heats
 * use ordering <code>3</code>, the four quarter-final heats use <code>4</code>, and so on.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class LowPointWithEliminationsAndRoundsWinnerGets07 extends LowPoint {
    private static final long serialVersionUID = -2318652113347853873L;
    
    private static int LOSERS_FINAL_FLEET_ORDERING = 2;

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.LOW_POINT_WITH_ELIMINATIONS_AND_ROUNDS_WINNER_GETS_07;
    }
    
    /**
     * A competitor will obtain a score in a column if she has no score in any of the subsequent columns of the same
     * elimination. Assuming that each series has a single race column only, the next column is identical to the single
     * race column of the next series. If the <code>raceColumn</code> is the last column (representing the last round)
     * of the elimination, the round is a final round, and all competitors participating in any of the races linked to
     * any of the fleets in that column are eligible to receive a score in that column.
     * <p>
     * 
     * The rationale behind this rule is that if a competitor got promoted to the next round of the elimination, no
     * score will be assigned to that competitor in the round from which she got promoted unless the next heat that
     * the competitor would have raced in is not sailed, in which case average points will be assigned to all competitors
     * promoted to the heat that did not get sailed.
     */
    private boolean competitorGetsScoreInColumn(Leaderboard leaderboard, RaceColumnInSeries raceColumn, Competitor competitor, TimePoint timePoint) {
        final Series series = raceColumn.getSeries();
        final Regatta regatta = series.getRegatta();
        boolean foundColumn = false;
        boolean result = true;
        // remember the ordering of any heat in raceColumn; the next column that has a greater ordering
        // belongs to the next elimination
        final int heatOrdering = raceColumn.getFleets().iterator().next().getOrdering();
        for (Series seriesInRegatta : regatta.getSeries()) {
            if (foundColumn && series.getFleets().iterator().next().getOrdering() > heatOrdering) {
                // reached next elimination; abort
                break;
            }
            if (!foundColumn && seriesInRegatta == series) {
                foundColumn = true;
            } else {
                if (foundColumn && hasScoreInSeries(leaderboard, competitor, seriesInRegatta, timePoint)) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    private boolean hasScoreInSeries(Leaderboard leaderboard, Competitor competitor, Series series, TimePoint timePoint) {
        final RaceColumn singleRaceColumnInSeries = series.getRaceColumns().iterator().next();
        return leaderboard.getNetPoints(competitor, singleRaceColumnInSeries, timePoint) != null;
    }

    @Override
    public Double getScoreForRank(final Leaderboard leaderboard, final RaceColumn raceColumn, final Competitor competitor, final int rank,
            final Callable<Integer> numberOfCompetitorsInRaceFetcher,
            final NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint) {
        assert raceColumn instanceof RaceColumnInSeries;
        final Double result;
        if (rank == 0) {
            result = null;
        } else if (isFinalRound((RaceColumnInSeries) raceColumn)) {
            final int effectiveRank = getEffectiveRank(raceColumn, competitor, rank);
            result = effectiveRank == 0 ? null : effectiveRank == 1 ? 0.7 : (double) effectiveRank;
        } else if (competitorGetsScoreInColumn(leaderboard, (RaceColumnInSeries) raceColumn, competitor, timePoint)) {
            // calculate the average rank or all competitors with the same rank in their respective heat in the round
            // to which raceColumn belongs:
            final int numberOfCompetitorsWithSameRankInRound = getNumberOfCompetitorsWithSameRankInRound((RaceColumnInSeries) raceColumn, competitor, rank);
            final int numberOfCompetitorsWithBetterScoresInRound = Util.size(((RaceColumnInSeries) raceColumn).getSeries().getFleets()) * (rank-1);
            final int bestOverallRankForRank = numberOfCompetitorsWithBetterScoresInRound+1;
            final int worstOverallRankForRank = numberOfCompetitorsWithBetterScoresInRound+numberOfCompetitorsWithSameRankInRound;
            final int numberOfRanks = worstOverallRankForRank-bestOverallRankForRank+1;
            result = (double) bestOverallRankForRank + ((double) numberOfRanks-1)/2.0
                    // now subtract the difference between 1.0 and 0.7 in case the first place is contained:
                    - (bestOverallRankForRank==1 ? (1.0-0.7)/numberOfRanks : 0);
        } else {
            result = null; // not in final round; competitor was promoted from raceColumn to the next round and has
            // a non-null score in a subsequent round of the elimination
        }
        return result;
    }

    /**
     * Assumption: no fleet has a number of competitors exceeding that of any other fleet in the same round by two or
     * more.
     * <p>
     * 
     * Background: an additional competitor will always be added to the fleet with the fewest competitors, implying
     * above rule.
     * <p>
     * 
     * Reason for assumption: with this, we can infer that there will be as many competitors ranking one rank better
     * than "rank" as there are fleets; were the assumption not fulfilled then there could be a fleet in the round that
     * has no competitor ranking one rank better. With this assumption we easily infer the best possible rank for all
     * competitors with "rank" which is the number of fleets in the round times "rank" plus 1.
     * <p>
     * 
     * Example: If rank is 7 and we have eight fleets, the best rank for all competitors with this rank will be 7*8+1 =
     * 57. The worst rank for all competitors with "rank" therefore can be calculated by the adding to the best rank the
     * number of competitors with this rank. We have to assume that this number may not be equal to the number of heats
     * in this round because with different heat sizes in a round the worst rank may be different for different heats
     * (e.g., 7 in some, and 8 in others, therefore fewer 8th ranks than 7th ranks will exist). This "uneven" case can
     * only apply if the competitor ranks last in the heat which is easy to exclude by looking at the tracked race, from
     * there the RaceDefinition and for that the number of competitors in that race.
     */
    private int getNumberOfCompetitorsWithSameRankInRound(final RaceColumnInSeries raceColumn, final Competitor competitor, final int rank) {
        final int numberOfCompetitorsWithSameRankInRound;
        final TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
        if (trackedRace == null) {
            // Can't determine number of competitors in heat; assuming equal fleet distribution
            numberOfCompetitorsWithSameRankInRound = getNumberOfFleets(raceColumn);
        } else {
            final boolean isWorstRank = rank == Util.size(trackedRace.getRace().getCompetitors());
            if (!isWorstRank) {
                numberOfCompetitorsWithSameRankInRound = getNumberOfFleets(raceColumn);
            } else {
                // for all other fleets in the round that have tracked races attached, we can count how many of them
                // have fewer than "rank" competitors; for fleets with no tracked race attached we then again assume
                // maximum competitor number
                int count = 0;
                for (final Fleet fleet : raceColumn.getSeries().getFleets()) {
                    final TrackedRace trackedRaceForFleet = raceColumn.getTrackedRace(fleet);
                    if (trackedRaceForFleet == null) {
                        count++; // by default assume that other race has at least as many competitors as the competitor's heat
                    } else {
                        if (Util.size(trackedRaceForFleet.getRace().getCompetitors()) >= rank) {
                            count++;
                        }
                    }
                }
                numberOfCompetitorsWithSameRankInRound = count;
            }
        }
        return numberOfCompetitorsWithSameRankInRound;
    }

    private int getNumberOfFleets(final RaceColumnInSeries raceColumn) {
        return Util.size(raceColumn.getSeries().getFleets());
    }

    private boolean isFinalRound(RaceColumnInSeries raceColumn) {
        return raceColumn.getFleets().iterator().next().getOrdering() <= LOSERS_FINAL_FLEET_ORDERING;
    }
}
