package com.sap.sailing.domain.persistence.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.DelayedLeaderboardCorrections;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Keeps a record of carried points and score corrections to be applied, keyed by the competitor names to which they
 * apply. This allows the {@link DomainObjectFactoryImpl} to set carried points and score corrections by competitor
 * names when the {@link TrackedRace}s haven't been associated yet with the leaderboard. The carried points and
 * score corrections are then assigned to the {@link Competitor}s once they show up by
 * {@link #addRace(TrackedRace, String, boolean) adding races} to this leaderboard.<p>
 * 
 * This object registers itself as a {@link RaceColumnListener} on the {@link Leaderboard} at construction time. It
 * therefore receives all updates to the linking structure between the leaderboard's {@link RaceColumn}s and the
 * {@link TrackedRace}s associated with them and can react accordingly to assign left-over corrections to the
 * correct, resolved competitor objects (see {@link #trackedRaceLinked(RaceColumn, Fleet, TrackedRace)}).
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class DelayedLeaderboardCorrectionsImpl implements DelayedLeaderboardCorrections, Serializable {
    private static final long serialVersionUID = 8824782847677232275L;
    private final Map<String, Integer> carriedPointsByCompetitorName;
    private final Map<String, Map<RaceColumn, MaxPointsReason>> maxPointsReasonsByCompetitorName;
    private final Map<String, Map<RaceColumn, Integer>> correctedScoresByCompetitorName;
    private final Map<String, String> displayNamesByCompetitorName;
    private final Leaderboard leaderboard;

    public DelayedLeaderboardCorrectionsImpl(Leaderboard leaderboard) {
        carriedPointsByCompetitorName = new HashMap<String, Integer>();
        maxPointsReasonsByCompetitorName = new HashMap<String, Map<RaceColumn,MaxPointsReason>>();
        correctedScoresByCompetitorName = new HashMap<String, Map<RaceColumn,Integer>>();
        displayNamesByCompetitorName = new HashMap<String, String>();
        this.leaderboard = leaderboard;
        leaderboard.addRaceColumnListener(this);
    }
    
    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
    
    private void assertNoTrackedRaceAssociatedYet() {
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            if (raceColumn.hasTrackedRaces()) {
                throw new IllegalStateException("Can't enqueue competitor name-based state while tracked races are already associated with leaderboard");
            }
        }
    }

    @Override
    public void setCarriedPoints(String competitorName, int carriedPoints) {
        assertNoTrackedRaceAssociatedYet();
        carriedPointsByCompetitorName.put(competitorName, carriedPoints);
    }
    
    @Override
    public void setMaxPointsReason(String competitorName, RaceColumn raceColumn, MaxPointsReason maxPointsReason) {
        assertNoTrackedRaceAssociatedYet();
        Map<RaceColumn, MaxPointsReason> map = maxPointsReasonsByCompetitorName.get(competitorName);
        if (map == null) {
            map = new HashMap<RaceColumn, MaxPointsReason>();
            maxPointsReasonsByCompetitorName.put(competitorName, map);
        }
        map.put(raceColumn, maxPointsReason);
    }

    @Override
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
                leaderboard.setCarriedPoints(competitorsByName.get(carryEntry.getKey()), carryEntry.getValue());
                carryEntryIter.remove();
            }
        }
        for (Iterator<java.util.Map.Entry<String, Map<RaceColumn, MaxPointsReason>>> maxPointsReasonsEntryIter =
                maxPointsReasonsByCompetitorName.entrySet().iterator(); maxPointsReasonsEntryIter.hasNext();) {
            java.util.Map.Entry<String, Map<RaceColumn, MaxPointsReason>> maxPointsReasonEntries = maxPointsReasonsEntryIter.next();
            if (competitorsByName.containsKey(maxPointsReasonEntries.getKey())) {
                for (Map.Entry<RaceColumn, MaxPointsReason> maxPointsReasonEntry : maxPointsReasonEntries.getValue().entrySet()) {
                    leaderboard.getScoreCorrection().setMaxPointsReason(competitorsByName.get(maxPointsReasonEntries.getKey()),
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
                    leaderboard.getScoreCorrection().correctScore(competitorsByName.get(correctedScoresEntries.getKey()),
                            correctedScoreEntry.getKey(), correctedScoreEntry.getValue());
                }
                correctedScoresEntryIter.remove();
            }
        }
        for (Iterator<java.util.Map.Entry<String, String>> displayNamesEntryIter =
                displayNamesByCompetitorName.entrySet().iterator(); displayNamesEntryIter.hasNext();) {
            java.util.Map.Entry<String, String> displayNamesEntry = displayNamesEntryIter.next();
            if (competitorsByName.containsKey(displayNamesEntry.getKey())) {
                leaderboard.setDisplayName(competitorsByName.get(displayNamesEntry.getKey()), displayNamesEntry.getValue());
                displayNamesEntryIter.remove();
            }
        }
    }

    @Override
    public void setDisplayName(String competitorName, String displayName) {
        assertNoTrackedRaceAssociatedYet();
        displayNamesByCompetitorName.put(competitorName, displayName);
    }

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        assignLeftOvers(trackedRace);
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
    }
}
