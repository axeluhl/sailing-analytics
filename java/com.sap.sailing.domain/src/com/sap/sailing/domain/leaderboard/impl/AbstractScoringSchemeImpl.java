package com.sap.sailing.domain.leaderboard.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public abstract class AbstractScoringSchemeImpl implements ScoringScheme {
    private static final long serialVersionUID = 6830414905539642446L;
    
    private class ScoreComparator implements Comparator<Double>, Serializable {
        private static final long serialVersionUID = -2767385186133743330L;
        
        private final boolean nullScoresAreBetter;
        
        public ScoreComparator(boolean nullScoresAreBetter) {
            this.nullScoresAreBetter = nullScoresAreBetter;
        }

        @Override
        public int compare(Double o1, Double o2) {
            // null means did not enlist in the race or race hasn't started for that competitor yet; null
            // sorts "worse" than non-null.
            int result;
            if (o1 == null) {
                if (o2 == null) {
                    result = 0;
                } else {
                    result = nullScoresAreBetter ? -1 : 1;
                }
            } else {
                if (o2 == null) {
                    result = nullScoresAreBetter ? 1 : -1;
                } else {
                    result = o1.compareTo(o2) * (isHigherBetter() ? -1 : 1);
                }
            }
            return result;
        }
    }
    
    private final boolean higherIsBetter;
    
    public AbstractScoringSchemeImpl(boolean higherIsBetter) {
        this.higherIsBetter = higherIsBetter;
    }    

    @Override
    public boolean isHigherBetter() {
        return higherIsBetter;
    }

    @Override
    public Comparator<Double> getScoreComparator(boolean nullScoresAreBetter) {
        return new ScoreComparator(nullScoresAreBetter);
    }
    
    /**
     * This default implementation handles the somewhat tricky case where a score is considered valid for the total score only
     * if for multiple unordered fleets in the race all fleets have raced or are currently racing. There have been controversial
     * discussions whether this is desired. Usually, subclasses will override such that a result is generally valid for the
     * total scores, and the {@link #compareByNumberOfRacesScored(int, int)} method has to just rank those better who ran
     * more races.
     */
    @Override
    public boolean isValidInNetScore(Leaderboard leaderboard, RaceColumn raceColumn, Competitor competitor, TimePoint at) {
        boolean result;
        Iterable<? extends Fleet> fleets = raceColumn.getFleets();
        if (Util.size(fleets) <= 1 || allFleetsOrdered(fleets)) {
            result = true;
        } else {
            // multiple unordered fleets; ensure that the leaderboard has results for all of them
            result = leaderboardHasResultsForAllFleets(leaderboard, raceColumn, at);
        }
        return result;
    }

    private boolean leaderboardHasResultsForAllFleets(Leaderboard leaderboard, RaceColumn raceColumn, TimePoint at) {
        Set<Fleet> fleetsForWhichNoScoreWasFound = new HashSet<Fleet>();
        for (Fleet fleet : raceColumn.getFleets()) {
            final TrackedRace trackedRaceForFleet = raceColumn.getTrackedRace(fleet);
            if (trackedRaceForFleet == null || !trackedRaceForFleet.hasStarted(at)) {
                fleetsForWhichNoScoreWasFound.add(fleet);
            }
        }
        for (Competitor competitor : leaderboard.getCompetitors()) {
            Fleet fleet = raceColumn.getFleetOfCompetitor(competitor);
            if (fleetsForWhichNoScoreWasFound.contains(fleet)) {
                if (leaderboard.getTotalPoints(competitor, raceColumn, at) != null) {
                    fleetsForWhichNoScoreWasFound.remove(fleet);
                }
            }
        }
        return fleetsForWhichNoScoreWasFound.isEmpty();
    }

    private boolean allFleetsOrdered(Iterable<? extends Fleet> fleets) {
        boolean allOrdered = true;
        for (Fleet fleet : fleets) {
            if (fleet.getOrdering() == 0) {
                allOrdered = false;
                break;
            }
        }
        return allOrdered;
    }

    /**
     * Assuming both competitors scored in the same number of races, compares the sorted scores based on World Sailing's
     * Racing Rules of Sailing (RRS) addendum A8.1:<p>
     * 
     * <em>"A8.1 If there is a series-score tie between two or more boats, each boat’s race scores shall be listed in
     * order of best to worst, and at the first point(s) where there is a difference the tie shall be broken in favour
     * of the boat(s) with the best score(s). No excluded scores shall be used."</em>
     */
    @Override
    public int compareByBetterScore(Competitor o1, List<com.sap.sse.common.Util.Pair<RaceColumn, Double>> o1Scores,
            Competitor o2, List<com.sap.sse.common.Util.Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter,
            TimePoint timePoint, Leaderboard leaderboard, Map<Competitor, Set<RaceColumn>> discardedRaceColumnsPerCompetitor) {
        final Comparator<Pair<RaceColumn, Double>> ruleA8_1ScoreComparator = getRuleA8_1ScoreComparator(nullScoresAreBetter);
        // needs to compare net points; therefore, divide the total points by the column factor for comparison:
        List<Pair<RaceColumn, Double>> o1NetScores = new ArrayList<>();
        final Set<RaceColumn> o1Discards = discardedRaceColumnsPerCompetitor.get(o1);
        for (com.sap.sse.common.Util.Pair<RaceColumn, Double> o1ColumnAndScore : o1Scores) {
            if (!o1Discards.contains(o1ColumnAndScore.getA())) {
                o1NetScores.add(new Pair<>(o1ColumnAndScore.getA(), o1ColumnAndScore.getB() / getScoreFactor(o1ColumnAndScore.getA())));
            }
        }
        List<Pair<RaceColumn, Double>> o2NetScores = new ArrayList<>();
        final Set<RaceColumn> o2Discards = discardedRaceColumnsPerCompetitor.get(o2);
        for (com.sap.sse.common.Util.Pair<RaceColumn, Double> o2ColumnAndScore : o2Scores) {
            if (!o2Discards.contains(o2ColumnAndScore.getA())) {
                o2NetScores.add(new Pair<>(o2ColumnAndScore.getA(), o2ColumnAndScore.getB() / getScoreFactor(o2ColumnAndScore.getA())));
            }
        }
        Collections.sort(o1NetScores, ruleA8_1ScoreComparator);
        Collections.sort(o2NetScores, ruleA8_1ScoreComparator);
        // now both lists are sorted from best to worst score
        Iterator<Pair<RaceColumn, Double>> o1Iter = o1NetScores.iterator();
        Iterator<Pair<RaceColumn, Double>> o2Iter = o2NetScores.iterator();
        int result = 0;
        while (result == 0 && o1Iter.hasNext() && o2Iter.hasNext()) {
            result = getScoreComparator(nullScoresAreBetter).compare(o1Iter.next().getB(), o2Iter.next().getB());
        }
        if (o1Iter.hasNext() != o2Iter.hasNext()) {
            // if, as may be allowed by some scoring scheme variants, competitors with different numbers of scored races are compared
            // and are equal for all races of the competitor who scored fewer races, the competitor who scored more races is preferred
            result = o1Iter.hasNext() ? -1 : 1;
        }
        return result;
    }

    /**
     * Obtains a comparator that compares two non-discarded scores according the rule A8.1, or any
     * modification thereof (which subclasses can provide by overriding this method). This default implementation
     * simply uses the {@link #getScoreComparator(boolean) score comparator} to compare the net scores
     * directly.
     */
    protected Comparator<Pair<RaceColumn, Double>> getRuleA8_1ScoreComparator(boolean nullScoresAreBetter) {
        return (p1, p2)->getScoreComparator(nullScoresAreBetter).compare(p1.getB(), p2.getB());
    }

    /**
     * By default, this scoring scheme implementation does not compare the numbers of races scored.
     */
    @Override
    public int compareByNumberOfRacesScored(int competitor1NumberOfRacesScored, int competitor2NumberOfRacesScored) {
        return 0;
    }

    /**
     * Looks backwards starting at the last race until the first score difference is found, including the discarded
     * scored. This implements Racing Rules of Sailing (RRS) section A8.2:
     * <p>
     * 
     * <em>"A8.2 If a tie remains between two or more boats, they shall be ranked in order of their scores in the last race.
     * Any remaining ties shall be broken by using the tied boats’ scores in the next-to-last race and so on until all
     * ties are broken. These scores shall be used even if some of them are excluded scores."</em>
     * 
     * @param o1ScoresIncludingDiscarded
     *            scores that include the points for those races that have been discarded (total points)
     * @param o2ScoresIncludingDiscarded
     *            scores that include the points for those races that have been discarded (total points)
     */
    @Override
    public int compareByLastRace(List<com.sap.sse.common.Util.Pair<RaceColumn, Double>> o1ScoresIncludingDiscarded,
            List<com.sap.sse.common.Util.Pair<RaceColumn, Double>> o2ScoresIncludingDiscarded, boolean nullScoresAreBetter, Competitor o1, Competitor o2) {
        int result = 0;
        final Comparator<Double> pureScoreComparator = getScoreComparator(nullScoresAreBetter);
        ListIterator<Pair<RaceColumn, Double>> o1Iter = o1ScoresIncludingDiscarded.listIterator(o1ScoresIncludingDiscarded.size());
        ListIterator<Pair<RaceColumn, Double>> o2Iter = o2ScoresIncludingDiscarded.listIterator(o2ScoresIncludingDiscarded.size());
        while (result == 0 && o1Iter.hasPrevious() && o2Iter.hasPrevious()) {
            result = pureScoreComparator.compare(o1Iter.previous().getB(), o2Iter.previous().getB());
        }
        if (o1Iter.hasPrevious() != o2Iter.hasPrevious()) {
            // if, as may be allowed by some scoring scheme variants, competitors with different numbers of scored races are compared
            // and are equal for all races of the competitor who scored fewer races, the competitor who scored more races is preferred
            result = o1Iter.hasNext() ? -1 : 1;
        }
        return result;
    }

    protected int getNumberOfCompetitorsInBetterFleets(RaceColumn raceColumn, int competitorFleetOrdering) {
        int numberOfCompetitorsInBetterFleets = 0;
        for (Fleet fleet : raceColumn.getFleets()) {
            TrackedRace trackedRaceOfBetterFleet;
            if (fleet.getOrdering() != 0 && fleet.getOrdering() < competitorFleetOrdering &&
                    (trackedRaceOfBetterFleet=raceColumn.getTrackedRace(fleet)) != null) {
                numberOfCompetitorsInBetterFleets += Util.size(trackedRaceOfBetterFleet.getRace().getCompetitors());
            }
        }
        return numberOfCompetitorsInBetterFleets;
    }

    @Override
    public int compareByLatestRegattaInMetaLeaderboard(Leaderboard leaderboard, Competitor o1, Competitor o2, TimePoint timePoint) {
        return 0;
    }
}
