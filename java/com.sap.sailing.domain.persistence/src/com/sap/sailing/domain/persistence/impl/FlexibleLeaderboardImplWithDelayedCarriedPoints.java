package com.sap.sailing.domain.persistence.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.RaceColumnImpl;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Keeps a record of carried points and score corrections to be applied, keyed by the competitor names to which they
 * apply. This allows the {@link DomainObjectFactoryImpl} to set carried points and score corrections by competitor
 * names when the {@link TrackedRace}s haven't been associated yet with the leaderboard. The carried points and
 * score corrections are then assigned to the {@link Competitor}s once they show up by
 * {@link #addRace(TrackedRace, String, boolean) adding races} to this leaderboard.<p>
 * 
 * TODO This class should be in the impl package. However, in order to temporarily solve bug 595, moving this class
 * to the exported package may help. A better solution would involve obtaining this bundle's class loader in
 * <code>ObjectInputStreamResolvingAgainstDomainFactory</code> which is able to obtain the class from a non-exported
 * package.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class FlexibleLeaderboardImplWithDelayedCarriedPoints extends FlexibleLeaderboardImpl {
    private static final long serialVersionUID = -8933075542228571746L;
    private final Map<String, Integer> carriedPointsByCompetitorName;
    private final Map<String, Map<RaceColumn, MaxPointsReason>> maxPointsReasonsByCompetitorName;
    private final Map<String, Map<RaceColumn, Integer>> correctedScoresByCompetitorName;
    private final Map<String, String> displayNamesByCompetitorName;

    /**
     * A wrapper for {@link RaceColumn} that, when its {@link #setTrackedRace(Fleet, TrackedRace)} method is called,
     * additionally calls {@link FlexibleLeaderboardImplWithDelayedCarriedPoints#assignLeftOvers(TrackedRace)}.
     * 
     * @author Axel Uhl (D043530)
     */
    private class RaceColumnForDelayedCarriedPoints extends RaceColumnImpl {
        private static final long serialVersionUID = -1243132535406059096L;

        public RaceColumnForDelayedCarriedPoints(Leaderboard leaderboard, String name, boolean medalRace, Iterable<Fleet> fleets) {
            super(name, medalRace, fleets);
        }

        @Override
        public void setTrackedRace(Fleet fleet, TrackedRace trackedRace) {
            super.setTrackedRace(fleet, trackedRace);
            if (trackedRace != null) {
                assignLeftOvers(trackedRace);
            }
        }
    }
    
    public FlexibleLeaderboardImplWithDelayedCarriedPoints(String name, SettableScoreCorrection scoreCorrection,
            ThresholdBasedResultDiscardingRule resultDiscardingRule, Comparator<Integer> scoreComparator) {
        super(name, scoreCorrection, resultDiscardingRule, scoreComparator);
        carriedPointsByCompetitorName = new HashMap<String, Integer>();
        maxPointsReasonsByCompetitorName = new HashMap<String, Map<RaceColumn,MaxPointsReason>>();
        correctedScoresByCompetitorName = new HashMap<String, Map<RaceColumn,Integer>>();
        displayNamesByCompetitorName = new HashMap<String, String>();
    }
    
    private void assertNoTrackedRaceAssociatedYet() {
        for (RaceColumn raceColumn : getRaceColumns()) {
            if (raceColumn.hasTrackedRaces()) {
                throw new IllegalStateException("Can't enqueue competitor name-based state while tracked races are already associated with leaderboard");
            }
        }
    }

    @Override
    protected RaceColumnImpl createRaceColumn(String columnName, boolean medalRace, Fleet... fleets) {
        return new RaceColumnForDelayedCarriedPoints(this, columnName, medalRace, turnNullOrEmptyFleetsIntoDefaultFleet(fleets));
    }

    public void setCarriedPoints(String competitorName, int carriedPoints) {
        assertNoTrackedRaceAssociatedYet();
        carriedPointsByCompetitorName.put(competitorName, carriedPoints);
    }
    
    public void setMaxPointsReason(String competitorName, RaceColumn raceColumn, MaxPointsReason maxPointsReason) {
        assertNoTrackedRaceAssociatedYet();
        Map<RaceColumn, MaxPointsReason> map = maxPointsReasonsByCompetitorName.get(competitorName);
        if (map == null) {
            map = new HashMap<RaceColumn, MaxPointsReason>();
            maxPointsReasonsByCompetitorName.put(competitorName, map);
        }
        map.put(raceColumn, maxPointsReason);
    }

    public void correctScore(String competitorName, RaceColumn raceColumn, int correctedScore) {
        assertNoTrackedRaceAssociatedYet();
        Map<RaceColumn, Integer> map = correctedScoresByCompetitorName.get(competitorName);
        if (map == null) {
            map = new HashMap<RaceColumn, Integer>();
            correctedScoresByCompetitorName.put(competitorName, map);
        }
        map.put(raceColumn, correctedScore);
    }

    /**
     * Performs the regular {@link FlexibleLeaderboardImpl#addRace(TrackedRace, String, boolean)} and then checks if
     * there are any carried points etc. left over to assign to the competitor objects (see
     * {@link #assignLeftOvers(TrackedRace)}).
     */
    @Override
    public RaceColumn addRace(TrackedRace race, String columnName, boolean medalRace, Fleet fleet) {
        RaceColumn result = super.addRace(race, columnName, medalRace, fleet);
        assignLeftOvers(race);
        return result;
    }

    /**
     * Checks if there are any carried points, max points reasons or corrected scores left over that may now receive
     * their competitor record. If so, {@link #setCarriedPoints(com.sap.sailing.domain.base.Competitor, int)},
     * {@link #setMaxPointsReason(String, RaceColumn, MaxPointsReason)} and/or
     * {@link #correctedScoresByCompetitorName} is invoked respectively, based on the data from
     * {@link #carriedPointsByCompetitorName}, {@link #maxPointsReasonsByCompetitorName} and
     * {@link #correctedScoresByCompetitorName}, respectively, which is removed afterwards.
     */
    private void assignLeftOvers(TrackedRace race) {
        Map<String, Competitor> competitorsByName = new HashMap<String, Competitor>();
        for (Competitor competitor : race.getRace().getCompetitors()) {
            competitorsByName.put(competitor.getName(), competitor);
        }
        for (Iterator<Map.Entry<String, Integer>> carryEntryIter = carriedPointsByCompetitorName.entrySet().iterator(); carryEntryIter.hasNext(); ) {
            Map.Entry<String, Integer> carryEntry = carryEntryIter.next();
            if (competitorsByName.containsKey(carryEntry.getKey())) {
                setCarriedPoints(competitorsByName.get(carryEntry.getKey()), carryEntry.getValue());
                carryEntryIter.remove();
            }
        }
        for (Iterator<java.util.Map.Entry<String, Map<RaceColumn, MaxPointsReason>>> maxPointsReasonsEntryIter =
                maxPointsReasonsByCompetitorName.entrySet().iterator(); maxPointsReasonsEntryIter.hasNext();) {
            java.util.Map.Entry<String, Map<RaceColumn, MaxPointsReason>> maxPointsReasonEntries = maxPointsReasonsEntryIter.next();
            if (competitorsByName.containsKey(maxPointsReasonEntries.getKey())) {
                for (Map.Entry<RaceColumn, MaxPointsReason> maxPointsReasonEntry : maxPointsReasonEntries.getValue().entrySet()) {
                    getScoreCorrection().setMaxPointsReason(competitorsByName.get(maxPointsReasonEntries.getKey()),
                            maxPointsReasonEntry.getKey(), maxPointsReasonEntry.getValue());
                }
                maxPointsReasonsEntryIter.remove();
            }
        }
        for (Iterator<java.util.Map.Entry<String, Map<RaceColumn, Integer>>> correctedScoresEntryIter =
                correctedScoresByCompetitorName.entrySet().iterator(); correctedScoresEntryIter.hasNext();) {
            java.util.Map.Entry<String, Map<RaceColumn, Integer>> correctedScoresEntries = correctedScoresEntryIter.next();
            if (competitorsByName.containsKey(correctedScoresEntries.getKey())) {
                for (java.util.Map.Entry<RaceColumn, Integer> correctedScoreEntry : correctedScoresEntries.getValue().entrySet()) {
                    getScoreCorrection().correctScore(competitorsByName.get(correctedScoresEntries.getKey()),
                            correctedScoreEntry.getKey(), correctedScoreEntry.getValue());
                }
                correctedScoresEntryIter.remove();
            }
        }
        for (Iterator<java.util.Map.Entry<String, String>> displayNamesEntryIter =
                displayNamesByCompetitorName.entrySet().iterator(); displayNamesEntryIter.hasNext();) {
            java.util.Map.Entry<String, String> displayNamesEntry = displayNamesEntryIter.next();
            if (competitorsByName.containsKey(displayNamesEntry.getKey())) {
                setDisplayName(competitorsByName.get(displayNamesEntry.getKey()), displayNamesEntry.getValue());
                displayNamesEntryIter.remove();
            }
        }
    }

    public void setDisplayName(String competitorName, String displayName) {
        assertNoTrackedRaceAssociatedYet();
        displayNamesByCompetitorName.put(competitorName, displayName);
    }
}
