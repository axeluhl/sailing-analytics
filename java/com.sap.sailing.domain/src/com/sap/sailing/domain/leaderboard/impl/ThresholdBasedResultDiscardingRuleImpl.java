package com.sap.sailing.domain.leaderboard.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindError;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sse.common.TimePoint;

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
public class ThresholdBasedResultDiscardingRuleImpl implements ThresholdBasedResultDiscardingRule {
    private static final long serialVersionUID = 386341628517357988L;
    private final int[] discardIndexResultsStartingWithHowManyRaces;
    
    public ThresholdBasedResultDiscardingRuleImpl(int[] discardIndexResultsStartingWithHowManyRaces) {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(discardIndexResultsStartingWithHowManyRaces);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ThresholdBasedResultDiscardingRuleImpl other = (ThresholdBasedResultDiscardingRuleImpl) obj;
        if (!Arrays.equals(discardIndexResultsStartingWithHowManyRaces,
                other.discardIndexResultsStartingWithHowManyRaces))
            return false;
        return true;
    }

    @Override
    public Set<RaceColumn> getDiscardedRaceColumns(final Competitor competitor, final Leaderboard leaderboard,
            Iterable<RaceColumn> raceColumnsToConsider, final TimePoint timePoint) {
        int resultsToDiscard = getNumberOfResultsToDiscard(competitor, raceColumnsToConsider, leaderboard, timePoint);
        Set<RaceColumn> result;
        if (resultsToDiscard > 0) {
            final Map<RaceColumn, Double> netPointsForCompetitorPerColumn = new HashMap<>();
            List<RaceColumn> sortedRaces = new ArrayList<RaceColumn>();
            for (RaceColumn raceColumn : raceColumnsToConsider) {
                if (raceColumn.isDiscardable()) {
                    try {
                        sortedRaces.add(raceColumn);
                        netPointsForCompetitorPerColumn.put(raceColumn, leaderboard.getNetPoints(competitor, raceColumn, timePoint));
                    } catch (NoWindException e) {
                        throw new NoWindError(e);
                    }
                }
            }
            result = new HashSet<RaceColumn>();
            Comparator<RaceColumn> comparator = new Comparator<RaceColumn>() {
                @Override
                public int compare(RaceColumn raceColumn1, RaceColumn raceColumn2) {
                    // invert to get bad races first; have the score comparator sort null scores as "better" so they end
                    // up at the end of the list
                    return -leaderboard
                            .getScoringScheme()
                            .getScoreComparator(/* nullScoresAreBetter */true)
                            .compare(netPointsForCompetitorPerColumn.get(raceColumn1),
                                    netPointsForCompetitorPerColumn.get(raceColumn2));
                }
            };
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
