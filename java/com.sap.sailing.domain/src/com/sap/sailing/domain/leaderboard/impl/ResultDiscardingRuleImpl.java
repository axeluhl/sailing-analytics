package com.sap.sailing.domain.leaderboard.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.tracking.NoWindError;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedRace;

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
    public Set<TrackedRace> getDiscardedRaces(final Competitor competitor, Iterable<TrackedRace> races, final TimePoint timePoint) {
        int resultsToDiscard = getNumberOfResultsToDiscard(races, timePoint);
        Set<TrackedRace> result;
        if (resultsToDiscard > 0) {
            result = new HashSet<TrackedRace>();
            TreeSet<TrackedRace> sortedRaces = new TreeSet<TrackedRace>(new Comparator<TrackedRace>() {
                @Override
                public int compare(TrackedRace o1, TrackedRace o2) {
                    try {
                        return o1.getRank(competitor, timePoint) - o2.getRank(competitor, timePoint);
                    } catch (NoWindException e) {
                        throw new NoWindError(e);
                    }
                }
            });
            for (TrackedRace race : races) {
                sortedRaces.add(race);
            }
            int i=0;
            Iterator<TrackedRace> badRacesIter = sortedRaces.descendingIterator();
            while (badRacesIter.hasNext() && i<resultsToDiscard) {
                result.add(badRacesIter.next());
                i++;
            }
        } else {
            result = Collections.emptySet();
        }
        return result;
    }

    private int getNumberOfResultsToDiscard(Iterable<TrackedRace> races, TimePoint timePoint) {
        int numberOfResultsToDiscard;
        int numberOfStartedRaces = 0;
        for (TrackedRace r : races) {
            if (r.hasStarted(timePoint)) {
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
