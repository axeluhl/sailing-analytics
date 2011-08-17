package com.sap.sailing.domain.leaderboard.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.tracking.NoWindError;
import com.sap.sailing.domain.tracking.NoWindException;

/**
 * Discards <code>i</code> results if the TODO TODO number of races started is greater or equal to
 * {@link #discardIndexResultsStartingWithHowManyRaces}<code>[i]</code> and
 * <code>i&lt;{@link #discardIndexResultsStartingWithHowManyRaces}.length</code>. If
 * <code>i&gt;={@link #discardIndexResultsStartingWithHowManyRaces}.length</code>, then
 * <code>{@link #discardIndexResultsStartingWithHowManyRaces}.length</code> results are discarded.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class ResultDiscardingRuleImpl implements ResultDiscardingRule {
    private final int[] discardIndexResultsStartingWithHowManyRaces;
    
    public ResultDiscardingRuleImpl(int[] discardIndexResultsStartingWithHowManyRaces) {
        super();
        this.discardIndexResultsStartingWithHowManyRaces = new int[discardIndexResultsStartingWithHowManyRaces.length];
        System.arraycopy(discardIndexResultsStartingWithHowManyRaces, 0,
                this.discardIndexResultsStartingWithHowManyRaces, 0, discardIndexResultsStartingWithHowManyRaces.length);
    }

    @Override
    public Set<RaceInLeaderboard> getDiscardedRaceColumns(final Competitor competitor, Iterable<RaceInLeaderboard> raceColumns, final TimePoint timePoint) {
        int resultsToDiscard = getNumberOfResultsToDiscard(raceColumns, timePoint);
        Set<RaceInLeaderboard> result;
        if (resultsToDiscard > 0) {
            result = new HashSet<RaceInLeaderboard>();
            TreeSet<RaceInLeaderboard> sortedRaces = new TreeSet<RaceInLeaderboard>(new Comparator<RaceInLeaderboard>() {
                @Override
                public int compare(RaceInLeaderboard o1, RaceInLeaderboard o2) {
                    try {
                        return o1.getTrackedRace().getRank(competitor, timePoint) - o2.getTrackedRace().getRank(competitor, timePoint);
                    } catch (NoWindException e) {
                        throw new NoWindError(e);
                    }
                }
            });
            for (RaceInLeaderboard raceColumn : raceColumns) {
                if (raceColumn.getTrackedRace() != null && !raceColumn.isMedalRace()) {
                    sortedRaces.add(raceColumn);
                }
            }
            int i=0;
            Iterator<RaceInLeaderboard> badRacesIter = sortedRaces.descendingIterator();
            while (badRacesIter.hasNext() && i<resultsToDiscard) {
                result.add(badRacesIter.next());
                i++;
            }
        } else {
            result = Collections.emptySet();
        }
        return result;
    }

    private int getNumberOfResultsToDiscard(Iterable<RaceInLeaderboard> raceColumns, TimePoint timePoint) {
        int numberOfResultsToDiscard;
        int numberOfStartedRaces = 0;
        for (RaceInLeaderboard raceInLeaderboard : raceColumns) {
            if (raceInLeaderboard.getTrackedRace() != null && raceInLeaderboard.getTrackedRace().hasStarted(timePoint)) {
                numberOfStartedRaces++;
            }
        }
        numberOfResultsToDiscard = 0;
        while (numberOfResultsToDiscard < discardIndexResultsStartingWithHowManyRaces.length
                && discardIndexResultsStartingWithHowManyRaces[numberOfResultsToDiscard] <= numberOfStartedRaces) {
            numberOfResultsToDiscard++;
        }
        return numberOfResultsToDiscard;
    }

}
