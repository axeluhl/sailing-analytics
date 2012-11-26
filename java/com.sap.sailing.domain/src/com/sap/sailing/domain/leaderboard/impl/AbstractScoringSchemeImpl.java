package com.sap.sailing.domain.leaderboard.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.tracking.TrackedRace;

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
    
    @Override
    public boolean isValidInTotalScore(Leaderboard leaderboard, RaceColumn raceColumn, TimePoint at) {
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
                try {
                    if (leaderboard.getNetPoints(competitor, raceColumn, at) != null) {
                        fleetsForWhichNoScoreWasFound.remove(fleet);
                    }
                } catch (NoWindException nwe) {
                    // can't occur here because no started tracked race exists yet for the competitor
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
     * Assuming both competitors scored in the same number of races, compares the sorted scores.
     */
    @Override
    public int compareByBetterScore(List<Double> o1Scores, List<Double> o2Scores, boolean nullScoresAreBetter) {
        assert o1Scores.size() == o2Scores.size();
        Comparator<Double> scoreComparator = getScoreComparator(nullScoresAreBetter);
        List<Double> o1ScoresCopy = new ArrayList<Double>(o1Scores);
        List<Double> o2ScoresCopy = new ArrayList<Double>(o2Scores);
        Collections.sort(o1ScoresCopy, scoreComparator);
        Collections.sort(o2ScoresCopy, scoreComparator);
        // now both lists are sorted from best to worst score
        Iterator<Double> o1Iter = o1ScoresCopy.iterator();
        Iterator<Double> o2Iter = o2ScoresCopy.iterator();
        int result = 0;
        while (result == 0 && o1Iter.hasNext() && o2Iter.hasNext()) {
            result = scoreComparator.compare(o1Iter.next(), o2Iter.next());
        }
        return result;
    }

    /**
     * By default, this scoring scheme implementation directly compares the numbers of races scored. A competitor who
     * scored fewer races is ranked worse ("greater") than a competitor with more races scored.
     */
    @Override
    public int compareByNumberOfRacesScored(int competitor1NumberOfRacesScored, int competitor2NumberOfRacesScored) {
        return competitor2NumberOfRacesScored - competitor1NumberOfRacesScored;
    }

    @Override
    public int compareByLastRace(List<Double> o1Scores, List<Double> o2Scores, boolean nullScoresAreBetter) {
        int result = 0;
        if (!o1Scores.isEmpty() && !o2Scores.isEmpty()) {
            result = getScoreComparator(nullScoresAreBetter).compare(o1Scores.get(o1Scores.size()-1), o2Scores.get(o2Scores.size()-1));
        }
        return result;
    }
}
