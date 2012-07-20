package com.sap.sailing.domain.leaderboard.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.NoWindError;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;

/**
 * Discards <code>i</code> results if the number of races started is greater or equal to
 * {@link #discardIndexResultsStartingWithHowManyRaces}<code>[i]</code> and
 * <code>i&lt;{@link #discardIndexResultsStartingWithHowManyRaces}.length</code>. If
 * <code>i&gt;={@link #discardIndexResultsStartingWithHowManyRaces}.length</code>, then
 * <code>{@link #discardIndexResultsStartingWithHowManyRaces}.length</code> results are discarded.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class ResultDiscardingRuleImpl implements ThresholdBasedResultDiscardingRule {
    private static final long serialVersionUID = 386341628517357988L;
    private final int[] discardIndexResultsStartingWithHowManyRaces;
    
    public ResultDiscardingRuleImpl(int[] discardIndexResultsStartingWithHowManyRaces) {
        super();
        this.discardIndexResultsStartingWithHowManyRaces = new int[discardIndexResultsStartingWithHowManyRaces.length];
        System.arraycopy(discardIndexResultsStartingWithHowManyRaces, 0,
                this.discardIndexResultsStartingWithHowManyRaces, 0, discardIndexResultsStartingWithHowManyRaces.length);
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
    }

    @Override
    public Set<RaceColumn> getDiscardedRaceColumns(final Competitor competitor, final Leaderboard leaderboard, final TimePoint timePoint) {
        int resultsToDiscard = getNumberOfResultsToDiscard(leaderboard.getRaceColumns(), leaderboard, timePoint);
        Set<RaceColumn> result;
        if (resultsToDiscard > 0) {
            result = new HashSet<RaceColumn>();
            TreeSet<RaceColumn> sortedRaces = new TreeSet<RaceColumn>(new Comparator<RaceColumn>() {
                @Override
                public int compare(RaceColumn o1, RaceColumn o2) {
                    try {
                        return Double.valueOf(leaderboard.getNetPoints(competitor, o1, timePoint)).compareTo(
                                Double.valueOf(leaderboard.getNetPoints(competitor, o2, timePoint)));
                    } catch (NoWindException e) {
                        throw new NoWindError(e);
                    }
                }
            });
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                if (!raceColumn.isMedalRace()) {
                    sortedRaces.add(raceColumn);
                }
            }
            int i=0;
            Iterator<RaceColumn> badRacesIter = sortedRaces.descendingIterator();
            while (badRacesIter.hasNext() && i<resultsToDiscard) {
                result.add(badRacesIter.next());
                i++;
            }
        } else {
            result = Collections.emptySet();
        }
        return result;
    }

    private int getNumberOfResultsToDiscard(Iterable<RaceColumn> raceColumns, Leaderboard leaderboard, TimePoint timePoint) {
        int numberOfResultsToDiscard;
        int numberOfStartedRaces = 0;
        for (RaceColumn raceInLeaderboard : raceColumns) {
            if (leaderboard.considerForDiscarding(raceInLeaderboard, timePoint)) {
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

    @Override
    public int[] getDiscardIndexResultsStartingWithHowManyRaces() {
        return discardIndexResultsStartingWithHowManyRaces;
    }
}
