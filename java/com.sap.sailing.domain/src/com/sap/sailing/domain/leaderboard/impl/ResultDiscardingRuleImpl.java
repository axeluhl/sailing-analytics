package com.sap.sailing.domain.leaderboard.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
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
    public Set<RaceColumn> getDiscardedRaceColumns(final Competitor competitor, final Leaderboard leaderboard,
            Iterable<RaceColumn> raceColumnsToConsider, final TimePoint timePoint) {
        int resultsToDiscard = getNumberOfResultsToDiscard(competitor, raceColumnsToConsider, leaderboard, timePoint);
        Set<RaceColumn> result;
        if (resultsToDiscard > 0) {
            result = new HashSet<RaceColumn>();
            List<RaceColumn> sortedRaces = new ArrayList<RaceColumn>();
            Comparator<RaceColumn> comparator = new Comparator<RaceColumn>() {
                @Override
                public int compare(RaceColumn o1, RaceColumn o2) {
                    try {
                        // invert to get bad races first; have the score comparator sort null scores as "better" so they end
                        // up at the end of the list
                        return -leaderboard
                                .getScoringScheme()
                                .getScoreComparator(/* nullScoresAreBetter */true)
                                .compare(leaderboard.getNetPoints(competitor, o1, timePoint),
                                        leaderboard.getNetPoints(competitor, o2, timePoint));
                    } catch (NoWindException e) {
                        throw new NoWindError(e);
                    }
                }
            };
            for (RaceColumn raceColumn : raceColumnsToConsider) {
                if (!raceColumn.isMedalRace()) {
                    sortedRaces.add(raceColumn);
                }
            }
            Collections.sort(sortedRaces, comparator);
            int i=0;
            Iterator<RaceColumn> badRacesIter = sortedRaces.iterator();
            while (badRacesIter.hasNext() && i<resultsToDiscard) {
                final RaceColumn badRace = badRacesIter.next();
                final MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, badRace, timePoint);
                if (maxPointsReason == null || maxPointsReason.isDiscardable()) {
                    result.add(badRace);
                    i++;
                }
            }
        } else {
            result = Collections.emptySet();
        }
        return result;
    }

    private int getNumberOfResultsToDiscard(Competitor competitor, Iterable<RaceColumn> raceColumnsToConsider,
            Leaderboard leaderboard, TimePoint timePoint) {
        int numberOfResultsToDiscard;
        int numberOfStartedRaces = 0;
        for (RaceColumn raceInLeaderboard : raceColumnsToConsider) {
            if (leaderboard.countRaceForComparisonWithDiscardingThresholds(competitor, raceInLeaderboard, timePoint)) {
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
