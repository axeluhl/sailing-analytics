package com.sap.sailing.domain.leaderboard.impl;

import java.util.Iterator;
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
 * "losers final" gets one point more than the competitor ranking last in the "final" race. The winner of the final race
 * obtains 0.7 points. All other ranks are assigned points equal to the rank or the average of the ranks attained by
 * competitors ranking equal.
 * <p>
 * 
 * The semi-final has two heats, and the better half of the competitors in those heats get promoted to the "final," the
 * competitors ranking worse will participate in the "losers final." Points are usually not awarded in the semi-final
 * (exceptions for races not sailed see below).
 * <p>
 * 
 * In the quarter-final there are four heats. Usually, the competitors ranking in the top half of their heat are
 * promoted to the semi-final and are not awarded points for the quarter-final round (exceptions for races not sailed
 * see below). However, the race committee may decide how many competitors are promoted, also based on the number of
 * participant and resulting number and set-up of heats. Therefore, successful promotion can only be detected by looking
 * at the competitor assignments in the next round.
 * <p>
 * 
 * Those competitors ranking in the bottom part of their heat are not promoted to the next round and are instead awarded
 * points for the elimination based on their rank in their heat. Across all quarter-final heats, competitors not
 * promoted and having the same rank obtain equal points regardless their heat. The number of points is calculated as
 * the average of their rank in the elimination. Quarter-final participants rank better than those eliminated in earlier
 * rounds. Eliminated competitors rank better in the elimination if they obtained a better rank in their heat than other
 * competitors eliminated in the same round. For example, if each of the four quarter-final heat has eight competitors,
 * the four competitors ranking last (8th) in their heat get the average points for ranks 29-32 (30.5 points), whereas
 * the four competitors ranking 7th in their heat get the average points for ranks 25-28 (26.5 points), and so on.
 * <p>
 * 
 * Other rounds preceding the quarter-final work by the same principle as the quarter-final.
 * <p>
 * 
 * Should a planned heat not be sailed then all competitors assigned to that heat will obtain equal points based on the
 * average of the ranks for which they would have sailed. For example, if the final heat cannot be sailed, all
 * competitors who qualified for the final race obtain the average of 0.7, 2, 3, 4, 5, 6, 7 and 8 points (4.46 points).
 * <p>
 * 
 * During live tracking promotions cannot generally be decided due to the race committee's decision authority.
 * Therefore, also those competitors who ultimately will be promoted have to be given scores in their heat based on the
 * average points obtained by all competitors taking the same rank in their respective heat in the same round. This will
 * usually lead to a change in points awarded to the competitors promoted as soon as the promotion becomes "official"
 * which technically needs to be expressed by assigning the competitors to their heat in the next round. As soon as this
 * happens, the scores in the heat from which a competitor got promoted are set to <code>null</code>, and a score
 * corresponding to the average points given to the competitors of the heat to which the competitor got promoted is
 * inferred for that heat. Only when the race in that next heat produces non-zero ranks, the scoring rules for that
 * heat are applied again normally.<p>
 * 
 * The regatta is expected to be modeled such that fleet ordering numbers are used in a special way. Ordering
 * <code>1</code> is to be used to identify the Final heat, <code>2</code> for the Losers Final heat. For each earlier
 * round the ordering is increased by one and applied equally to all heats of that round. Therefore, the two semi-final
 * heats use ordering <code>3</code>, the four quarter-final heats use <code>4</code>, and so on.
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
     * The competitor gets a score in <code>raceColumn</code> if she does not participate in a race in the round that
     * follows the round represented by <code>raceColumn</code>. If <code>raceColumn</code> represents the last round in
     * the elimination, <code>competitor</code> can not participate in a next round and therefore obtains a score. This
     * can either happen when <code>raceColumn</code> is a final round or if the elimination has been aborted and no
     * further rounds exist in that elimination. The competitor is considered to participate in the next round if an
     * explicit score correction exists for the <code>competitor</code> for the next round, or if a tracked race is
     * attached to the race column following <code>raceColumn</code> that lists <code>competitor</code> as part of its
     * competitors.
     * <p>
     * 
     * If the <code>competitor</code> is to receive points for <code>raceColumn</code>, the following rules apply. For a
     * <code>0</code> rank this method assumes that the race has not started yet (and it could happen that it is never
     * started at all); in this case the number of points is calculated as the arithmetic mean of all points for which
     * the participants of the heat would race, with the first rank counting as 1.0 for this average. If the
     * <code>rank</code> is not <code>0</code> then the competitor receives the average of the points given to all
     * competitors from all heats in this round that rank equal when sorting the competitors of that round in the
     * leaderboard by their rank. For example, if there are two heats in the semi-final with eight competitors each, the
     * two competitors ranking second in their heat will get the average of 3.0 and 4.0 points, so 3.5 points.<p>
     * 
     * In the exceptional case of the final round, 0.7 points are awarded to the first rank. This special rules only
     * applies if a <code>rank</code> 1 is actually observed. It does not apply to the averaging process that occurs
     * should the final not have been sailed (yet) such that the final participants receive the points average of
     * 1.0, 2.0, ..., &lt;<i>number-of-final-participants</i>&gt;<p>
     */
    @Override
    public Double getScoreForRank(final Leaderboard leaderboard, final RaceColumn raceColumn, final Competitor competitor, final int rank,
            final Callable<Integer> numberOfCompetitorsInRaceFetcher,
            final NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint) {
        assert raceColumn instanceof RaceColumnInSeries;
        // Clarified with Juergen Bonne in an e-mail as of 18-09-2015T09:03:00Z that a final race's winner
        // is scored with 0.7 only if the final race has actually been sailed. If the competitors are qualified
        // for the final race but it's not sailed, average scores are to be assigned to all competitors qualified
        // for the final race, but this average assumes 1.0 points for the first rank instead of the 0.7 assigned
        // to the winner if the race is actually sailed.
        final RaceColumnInSeries raceColumnInSeries = (RaceColumnInSeries) raceColumn;
        final Double result;
        final Series series = ((RaceColumnInSeries) raceColumn).getSeries();
        final Regatta regatta = series.getRegatta();
        Iterator<? extends Series> seriesInRegattaIter=regatta.getSeries().iterator();
        Series seriesInRegatta = null;
        while (seriesInRegattaIter.hasNext() && (seriesInRegatta=seriesInRegattaIter.next()) != series)
            ;
        if (seriesInRegatta == null) {
            throw new IllegalArgumentException("Race column "+raceColumn+" not found in its owning regatta");
        }
        final Fleet fleetOfCompetitor = raceColumn.getFleetOfCompetitor(competitor);
        if (fleetOfCompetitor == null || participatesInNextRound(leaderboard, raceColumnInSeries, competitor, timePoint, seriesInRegattaIter)) {
            // the competitor does not participate in this round according to the set of tracked races
            // attached to the raceColumn or obtains a score in the next round which means that the
            // competitor will not get a score yet for the raceColumn passed
            result = null;
        } else {
            assert fleetOfCompetitor != null;
            if (rank == 0) {
                // calculate the average rank of all competitors of the heat; for contiguous scoring across heats (final round)
                // this is the average of the points (giving 1.0 for first rank in the final) in the single heat; for rounds
                // with unordered heats (fleets all having equal ordering, non-contiguous series scoring) this is the average
                // of all the points assigned to all the ranks occupied in the elimination by the participants of this round.
                if (series.hasSplitFleetContiguousScoring()) {
                    // final round; check how many competitors are in better fleet; then average between (that number plus 1)
                    // and (that number plus the number of competitors in competitor's fleet)
                    final int competitorFleetOrdering = fleetOfCompetitor.getOrdering();
                    final int numberOfCompetitorsInBetterFleet = getNumberOfCompetitorsInBetterFleets(raceColumnInSeries, competitorFleetOrdering);
                    final TrackedRace finalTrackedRaceOfCompetitor = raceColumn.getTrackedRace(competitor); // assumed to be != null because fleetOfCompetitor != null
                    final int numberOfCompetitorsInCompetitorsFleet = Util.size(finalTrackedRaceOfCompetitor.getRace().getCompetitors());
                    result = ((double)numberOfCompetitorsInBetterFleet + 1.0 +   // best score in competitor's fleet
                              (double)numberOfCompetitorsInBetterFleet + (double)numberOfCompetitorsInCompetitorsFleet) // worst score in competitor's fleet
                              /2.0;
                } else {
                    // count competitors in this round and average the points, assuming that the participants that got promoted
                    // into this non-final round will race for scores 1.0 .. <number-of-competitors-in-the-round>.
                    int numberOfCompetitorsInRound = 0;
                    for (final Fleet fleet : raceColumn.getFleets()) {
                        final TrackedRace trackedRaceInRound = raceColumn.getTrackedRace(fleet);
                        if (trackedRaceInRound != null) {
                            numberOfCompetitorsInRound += Util.size(trackedRaceInRound.getRace().getCompetitors());
                        }
                    }
                    result = ((double) numberOfCompetitorsInRound + 1.0) / 2.0;
                }
            } else {
                // rank is non-zero; there is a tracked race that provides a valid score for the competitor
                final int effectiveRank = getEffectiveRank(raceColumn, competitor, rank);
                if (series.hasSplitFleetContiguousScoring()) {
                    result = effectiveRank == 1 ? 0.7 : effectiveRank;
                } else {
                    final int numberOfCompetitorsInBetterFleets = getNumberOfCompetitorsInBetterFleets(raceColumnInSeries, fleetOfCompetitor.getOrdering());
                    final int numberOfFleetsWithEqualOrder = getNumberOfFleetsWithOrdering(series, fleetOfCompetitor.getOrdering());
                    final int numberOfCompetitorsWithSameRankInRound = getNumberOfCompetitorsWithSameRankInRound(raceColumnInSeries, competitor, effectiveRank);
                    final int bestOverallRankForAnyEqualRankingCompetitor =
                            numberOfCompetitorsInBetterFleets + 1 + // best possible score for any competitor in competitor's fleet
                            numberOfFleetsWithEqualOrder*(effectiveRank-1); // number of places assigned to better competitors in own and other equal fleets
                    final int worstOverallRankForAnyEqualRankingCompetitor =
                            bestOverallRankForAnyEqualRankingCompetitor + numberOfCompetitorsWithSameRankInRound - 1;
                    result = ((double) bestOverallRankForAnyEqualRankingCompetitor + (double) worstOverallRankForAnyEqualRankingCompetitor) / 2.0;
                }
            }
        }
        return result;
    }

    @Override
    public boolean isValidInNetScore(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor, TimePoint at) {
        final RaceColumnInSeries raceColumnInSeries = (RaceColumnInSeries) raceColumn;
        final Iterator<? extends Series> seriesInRegattaIter = raceColumnInSeries.getSeries().getRegatta().getSeries().iterator();
        while (seriesInRegattaIter.hasNext() && seriesInRegattaIter.next() != raceColumnInSeries.getSeries())
            ;
        return !participatesInNextRound(leaderboard, (RaceColumnInSeries) raceColumn, competitor, at, seriesInRegattaIter);
    }

    private int getNumberOfFleetsWithOrdering(Series series, int ordering) {
        int result = 0;
        for (final Fleet fleet : series.getFleets()) {
            if (fleet.getOrdering() == ordering) {
                result++;
            }
        }
        return result;
    }

    /**
     * A competitor can participate in the next round of an elimination after having been promoted from
     * <code>raceColumnInSeries</code>. If the round represented by <code>raceColumnInSeries</code> is the last round in
     * the elimination, the competitor obviously cannot participate in any next round, therefore <code>false</code> is
     * returned in this case. Otherwise, next round participation is decided by first looking for a score correction.
     * The score correction for the next column has to be valid at <code>timePoint</code>, then the competitor is considered
     * having participated in that next round at <code>timePoint</code>. If no score correction is found for the next column,
     * the next column is checked for a tracked race in which <code>competitor</code> competes. If such a race is found,
     * the question remains whether this participation is considered "active" at <code>timePoint</code>. This is assumed
     * if in the current round represented by <code>raceColumnInSeries</code> no tracked race is found for <code>competitor</code>
     * (unlikely, but it means we cannot tell whether <code>competitor</code> has already finished her current race, and by
     * default we then have to assume that it's over) or the end of that race was before <code>timePoint</code>.<p>
     * 
     * This will keep <code>competitor</code>'s score in <code>raceColumnInSeries</code> for time points where the race
     * in this column is still running. Only after the race has finished will the promotion become active and competitor
     * lose her points in the current column.
     */
    private boolean participatesInNextRound(Leaderboard leaderboard, RaceColumnInSeries raceColumnInSeries,
            Competitor competitor, TimePoint timePoint, Iterator<? extends Series> seriesInRegattaIter) {
        final boolean result;
        if (isLastRoundInElimination(raceColumnInSeries, seriesInRegattaIter)) {
            result = false;
        } else {
            final Series nextSeries = seriesInRegattaIter.next();
            final TrackedRace trackedRace;
            result = leaderboard.getScoreCorrection().isScoreCorrected(competitor, nextSeries.getRaceColumns().iterator().next(), timePoint) ||
                    (nextSeries.getRaceColumns().iterator().next().getFleetOfCompetitor(competitor) != null &&
                     ((trackedRace=raceColumnInSeries.getTrackedRace(competitor)) == null || trackedRace.getEndOfRace() == null ||
                      trackedRace.getEndOfRace().before(timePoint)));
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
        if (raceColumn.getSeries().hasSplitFleetContiguousScoring()) {
            numberOfCompetitorsWithSameRankInRound = 1;
        } else {
            final Fleet fleet = raceColumn.getFleetOfCompetitor(competitor);
            if (fleet == null) {
                // Can't determine number of competitors in heat; assuming equal fleet distribution
                numberOfCompetitorsWithSameRankInRound = Util.size(raceColumn.getSeries().getFleets());
            } else {
                final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                final boolean isWorstRank = rank == Util.size(trackedRace.getRace().getCompetitors());
                if (!isWorstRank) {
                    numberOfCompetitorsWithSameRankInRound = getNumberOfFleetsWithSameOrderingAsCompetitor(raceColumn, competitor, fleet);
                } else {
                    // for all other fleets in the round that have tracked races attached, we can count how many of them
                    // have fewer than "rank" competitors; for fleets with no tracked race attached we then again assume
                    // maximum competitor number
                    int count = 0;
                    for (final Fleet f : raceColumn.getSeries().getFleets()) {
                        if (f.getOrdering() == fleet.getOrdering()) { // only consider fleets with equal order to that of competitor's
                            final TrackedRace trackedRaceForFleet = raceColumn.getTrackedRace(f);
                            if (trackedRaceForFleet == null) {
                                count++; // by default assume that other race has at least as many competitors as the competitor's heat
                            } else {
                                if (Util.size(trackedRaceForFleet.getRace().getCompetitors()) >= rank) {
                                    count++;
                                }
                            }
                        }
                    }
                    numberOfCompetitorsWithSameRankInRound = count;
                }
            }
        }
        return numberOfCompetitorsWithSameRankInRound;
    }

    private int getNumberOfFleetsWithSameOrderingAsCompetitor(final RaceColumnInSeries raceColumn, Competitor competitor, Fleet fleetOfCompetitor) {
        int result = 0;
        final int orderingOfCompetitorsFleet = fleetOfCompetitor.getOrdering();
        for (final Fleet fleet : raceColumn.getFleets()) {
            if (fleet.getOrdering() == orderingOfCompetitorsFleet) {
                result++;
            }
        }
        return result;
    }

    private boolean isLastRoundInElimination(RaceColumnInSeries raceColumn, Iterator<? extends Series> seriesInRegattaIter) {
        final boolean result;
        if (raceColumn.getFleets().iterator().next().getOrdering() <= LOSERS_FINAL_FLEET_ORDERING) {
            // the fleet ordering indicates that raceColumn represents a final round in an elimination
            result = true;
        } else {
            // check if there is a next column
            result = !seriesInRegattaIter.hasNext();
        }
        return result;
    }
}
