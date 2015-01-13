package com.sap.sailing.domain.leaderboard.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.DelayedLeaderboardCorrections;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.IsManagedByCache;

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
 * correct, resolved competitor objects (see {@link #trackedRaceLinked(RaceColumn, Fleet, TrackedRace)}). When
 * all left-overs have been applied to the leaderboard, this object removes itself as {@link RaceColumnListener} from
 * the leaderboard again.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class DelayedLeaderboardCorrectionsImpl implements DelayedLeaderboardCorrections, IsManagedByCache<SharedDomainFactory> {
    private static final long serialVersionUID = 8824782847677232275L;
    
    // deprecated structures that key corrections by name; to be removed once all databases have been migrated
    private final Map<String, Double> carriedPointsByCompetitorName;
    private final Map<String, Map<RaceColumn, MaxPointsReason>> maxPointsReasonsByCompetitorName;
    private final Map<String, Map<RaceColumn, Double>> correctedScoresByCompetitorName;
    private final Map<String, String> displayNamesByCompetitorName;
    private final Set<String> suppressedCompetitorNames;

    // structures that key corrections by competitor ID
    private final Map<Serializable, Double> carriedPointsByCompetitorID;
    private final Map<Serializable, Map<RaceColumn, MaxPointsReason>> maxPointsReasonsByCompetitorID;
    private final Map<Serializable, Map<RaceColumn, Double>> correctedScoresByCompetitorID;
    private final Map<Serializable, String> displayNamesByCompetitorID;
    private final Set<Serializable> suppressedCompetitorIDs;
    
    private final Leaderboard leaderboard;
    private transient Set<LeaderboardCorrectionsResolvedListener> listeners;

    private transient CompetitorFactory competitorFactory;
    
    public DelayedLeaderboardCorrectionsImpl(Leaderboard leaderboard, CompetitorFactory competitorFactory) {
        this.competitorFactory = competitorFactory;
        listeners = new HashSet<>();
        carriedPointsByCompetitorName = new HashMap<String, Double>();
        maxPointsReasonsByCompetitorName = new HashMap<String, Map<RaceColumn,MaxPointsReason>>();
        correctedScoresByCompetitorName = new HashMap<String, Map<RaceColumn, Double>>();
        displayNamesByCompetitorName = new HashMap<String, String>();
        suppressedCompetitorNames = new HashSet<String>();
        carriedPointsByCompetitorID = new HashMap<Serializable, Double>();
        maxPointsReasonsByCompetitorID = new HashMap<Serializable, Map<RaceColumn,MaxPointsReason>>();
        correctedScoresByCompetitorID = new HashMap<Serializable, Map<RaceColumn, Double>>();
        displayNamesByCompetitorID = new HashMap<Serializable, String>();
        suppressedCompetitorIDs = new HashSet<Serializable>();
        this.leaderboard = leaderboard;
        leaderboard.addRaceColumnListener(this);
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        listeners = new HashSet<>();
    }
    
    @Override
    public void addLeaderboardCorrectionsResolvedListener(LeaderboardCorrectionsResolvedListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void removeLeaderboardCorrectionsResolvedListener(LeaderboardCorrectionsResolvedListener listener) {
        listeners.remove(listener);
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
    public void setCarriedPointsByID(Serializable competitorId, double carriedPoints) {
        assertNoTrackedRaceAssociatedYet();
        Competitor competitor = competitorFactory.getExistingCompetitorById(competitorId);
        if (competitor != null) {
            leaderboard.setCarriedPoints(competitor, carriedPoints);
        } else {
            synchronized (carriedPointsByCompetitorID) {
                carriedPointsByCompetitorID.put(competitorId, carriedPoints);
            }
        }
    }
    
    @Override
    public void setCarriedPointsByName(String competitorName, double carriedPoints) {
        assertNoTrackedRaceAssociatedYet();
        synchronized (carriedPointsByCompetitorName) {
            carriedPointsByCompetitorName.put(competitorName, carriedPoints);
        }
    }

    @Override
    public void setMaxPointsReasonByID(Serializable competitorId, RaceColumn raceColumn, MaxPointsReason maxPointsReason) {
        assertNoTrackedRaceAssociatedYet();
        Competitor competitor = competitorFactory.getExistingCompetitorById(competitorId);
        if (competitor != null) {
            leaderboard.getScoreCorrection().setMaxPointsReason(competitor, raceColumn, maxPointsReason);
        } else {
            synchronized (maxPointsReasonsByCompetitorID) {
                Map<RaceColumn, MaxPointsReason> map = maxPointsReasonsByCompetitorID.get(competitorId);
                if (map == null) {
                    map = new HashMap<RaceColumn, MaxPointsReason>();
                    maxPointsReasonsByCompetitorID.put(competitorId, map);
                }
                map.put(raceColumn, maxPointsReason);
            }
        }
    }

    @Override
    public void setMaxPointsReasonByName(String competitorName, RaceColumn raceColumn, MaxPointsReason maxPointsReason) {
        assertNoTrackedRaceAssociatedYet();
        synchronized (maxPointsReasonsByCompetitorName) {
            Map<RaceColumn, MaxPointsReason> map = maxPointsReasonsByCompetitorName.get(competitorName);
            if (map == null) {
                map = new HashMap<RaceColumn, MaxPointsReason>();
                maxPointsReasonsByCompetitorName.put(competitorName, map);
            }
            map.put(raceColumn, maxPointsReason);
        }
    }

    @Override
    public void correctScoreByID(Serializable competitorId, RaceColumn raceColumn, double correctedScore) {
        assertNoTrackedRaceAssociatedYet();
        Competitor competitor = competitorFactory.getExistingCompetitorById(competitorId);
        if (competitor != null) {
            leaderboard.getScoreCorrection().correctScore(competitor, raceColumn, correctedScore);
        } else {
            synchronized (correctedScoresByCompetitorID) {
                Map<RaceColumn, Double> map = correctedScoresByCompetitorID.get(competitorId);
                if (map == null) {
                    map = new HashMap<RaceColumn, Double>();
                    correctedScoresByCompetitorID.put(competitorId, map);
                }
                map.put(raceColumn, correctedScore);
            }
        }
    }

    @Override
    public void correctScoreByName(String competitorName, RaceColumn raceColumn, double correctedScore) {
        assertNoTrackedRaceAssociatedYet();
        synchronized (correctedScoresByCompetitorName) {
            Map<RaceColumn, Double> map = correctedScoresByCompetitorName.get(competitorName);
            if (map == null) {
                map = new HashMap<RaceColumn, Double>();
                correctedScoresByCompetitorName.put(competitorName, map);
            }
            map.put(raceColumn, correctedScore);
        }
    }

    /**
     * Checks if there are any carried points, max points reasons or corrected scores left over that may now receive
     * their competitor record. If so, {@link #setCarriedPointsByName(com.sap.sailing.domain.base.Competitor, int)},
     * {@link #setMaxPointsReasonByName(String, RaceColumn, MaxPointsReason)} and/or
     * {@link #correctedScoresByCompetitorName} is invoked respectively, based on the data from
     * {@link #carriedPointsByCompetitorName}, {@link #maxPointsReasonsByCompetitorName} and
     * {@link #correctedScoresByCompetitorName}, respectively, which is removed afterwards.
     */
    private void assignLeftOvers(TrackedRace race) {
        Map<String, Competitor> competitorsByName = new HashMap<>();
        Map<Serializable, Competitor> competitorsByID = new HashMap<>();
        for (Competitor competitor : race.getRace().getCompetitors()) {
            competitorsByName.put(competitor.getName(), competitor);
            competitorsByID.put(competitor.getId(), competitor);
        }
        synchronized (carriedPointsByCompetitorID) {
            for (Iterator<Map.Entry<Serializable, Double>> carryEntryIter = carriedPointsByCompetitorID.entrySet()
                    .iterator(); carryEntryIter.hasNext();) {
                Map.Entry<Serializable, Double> carryEntry = carryEntryIter.next();
                if (competitorsByID.containsKey(carryEntry.getKey())) {
                    leaderboard.setCarriedPoints(competitorsByID.get(carryEntry.getKey()), carryEntry.getValue());
                    carryEntryIter.remove();
                }
            }
        }
        synchronized (carriedPointsByCompetitorName) {
            for (Iterator<Map.Entry<String, Double>> carryEntryIter = carriedPointsByCompetitorName.entrySet()
                    .iterator(); carryEntryIter.hasNext();) {
                Map.Entry<String, Double> carryEntry = carryEntryIter.next();
                if (competitorsByName.containsKey(carryEntry.getKey())) {
                    leaderboard.setCarriedPoints(competitorsByName.get(carryEntry.getKey()), carryEntry.getValue());
                    carryEntryIter.remove();
                }
            }
        }
        synchronized (maxPointsReasonsByCompetitorID) {
            for (Iterator<java.util.Map.Entry<Serializable, Map<RaceColumn, MaxPointsReason>>> maxPointsReasonsEntryIter = maxPointsReasonsByCompetitorID
                    .entrySet().iterator(); maxPointsReasonsEntryIter.hasNext();) {
                java.util.Map.Entry<Serializable, Map<RaceColumn, MaxPointsReason>> maxPointsReasonEntries = maxPointsReasonsEntryIter
                        .next();
                if (competitorsByID.containsKey(maxPointsReasonEntries.getKey())) {
                    for (Map.Entry<RaceColumn, MaxPointsReason> maxPointsReasonEntry : maxPointsReasonEntries
                            .getValue().entrySet()) {
                        leaderboard.getScoreCorrection().setMaxPointsReason(
                                competitorsByID.get(maxPointsReasonEntries.getKey()), maxPointsReasonEntry.getKey(),
                                maxPointsReasonEntry.getValue());
                    }
                    maxPointsReasonsEntryIter.remove();
                }
            }
        }
        synchronized (maxPointsReasonsByCompetitorName) {
            for (Iterator<java.util.Map.Entry<String, Map<RaceColumn, MaxPointsReason>>> maxPointsReasonsEntryIter = maxPointsReasonsByCompetitorName
                    .entrySet().iterator(); maxPointsReasonsEntryIter.hasNext();) {
                java.util.Map.Entry<String, Map<RaceColumn, MaxPointsReason>> maxPointsReasonEntries = maxPointsReasonsEntryIter
                        .next();
                if (competitorsByName.containsKey(maxPointsReasonEntries.getKey())) {
                    for (Map.Entry<RaceColumn, MaxPointsReason> maxPointsReasonEntry : maxPointsReasonEntries
                            .getValue().entrySet()) {
                        leaderboard.getScoreCorrection().setMaxPointsReason(
                                competitorsByName.get(maxPointsReasonEntries.getKey()), maxPointsReasonEntry.getKey(),
                                maxPointsReasonEntry.getValue());
                    }
                    maxPointsReasonsEntryIter.remove();
                }
            }
        }
        synchronized (correctedScoresByCompetitorID) {
            for (Iterator<java.util.Map.Entry<Serializable, Map<RaceColumn, Double>>> correctedScoresEntryIter = correctedScoresByCompetitorID
                    .entrySet().iterator(); correctedScoresEntryIter.hasNext();) {
                java.util.Map.Entry<Serializable, Map<RaceColumn, Double>> correctedScoresEntries = correctedScoresEntryIter
                        .next();
                if (competitorsByID.containsKey(correctedScoresEntries.getKey())) {
                    for (java.util.Map.Entry<RaceColumn, Double> correctedScoreEntry : correctedScoresEntries
                            .getValue().entrySet()) {
                        leaderboard.getScoreCorrection().correctScore(
                                competitorsByID.get(correctedScoresEntries.getKey()), correctedScoreEntry.getKey(),
                                correctedScoreEntry.getValue());
                    }
                    correctedScoresEntryIter.remove();
                }
            }
        }
        synchronized (correctedScoresByCompetitorName) {
            for (Iterator<java.util.Map.Entry<String, Map<RaceColumn, Double>>> correctedScoresEntryIter = correctedScoresByCompetitorName
                    .entrySet().iterator(); correctedScoresEntryIter.hasNext();) {
                java.util.Map.Entry<String, Map<RaceColumn, Double>> correctedScoresEntries = correctedScoresEntryIter
                        .next();
                if (competitorsByName.containsKey(correctedScoresEntries.getKey())) {
                    for (java.util.Map.Entry<RaceColumn, Double> correctedScoreEntry : correctedScoresEntries
                            .getValue().entrySet()) {
                        leaderboard.getScoreCorrection().correctScore(
                                competitorsByName.get(correctedScoresEntries.getKey()), correctedScoreEntry.getKey(),
                                correctedScoreEntry.getValue());
                    }
                    correctedScoresEntryIter.remove();
                }
            }
        }
        synchronized (displayNamesByCompetitorID) {
            for (Iterator<java.util.Map.Entry<Serializable, String>> displayNamesEntryIter = displayNamesByCompetitorID
                    .entrySet().iterator(); displayNamesEntryIter.hasNext();) {
                java.util.Map.Entry<Serializable, String> displayNamesEntry = displayNamesEntryIter.next();
                if (competitorsByID.containsKey(displayNamesEntry.getKey())) {
                    leaderboard.setDisplayName(competitorsByID.get(displayNamesEntry.getKey()),
                            displayNamesEntry.getValue());
                    displayNamesEntryIter.remove();
                }
            }
        }
        synchronized (displayNamesByCompetitorName) {
            for (Iterator<java.util.Map.Entry<String, String>> displayNamesEntryIter = displayNamesByCompetitorName
                    .entrySet().iterator(); displayNamesEntryIter.hasNext();) {
                java.util.Map.Entry<String, String> displayNamesEntry = displayNamesEntryIter.next();
                if (competitorsByName.containsKey(displayNamesEntry.getKey())) {
                    leaderboard.setDisplayName(competitorsByName.get(displayNamesEntry.getKey()),
                            displayNamesEntry.getValue());
                    displayNamesEntryIter.remove();
                }
            }
        }
        synchronized (suppressedCompetitorIDs) {
            for (Iterator<Serializable> suppressedCompetitorNameIter = suppressedCompetitorIDs.iterator(); suppressedCompetitorNameIter
                    .hasNext();) {
                Serializable next = suppressedCompetitorNameIter.next();
                if (competitorsByID.containsKey(next)) {
                    leaderboard.setSuppressed(competitorsByID.get(next), true);
                    suppressedCompetitorNameIter.remove();
                }
            }
        }
        synchronized (suppressedCompetitorNames) {
            for (Iterator<String> suppressedCompetitorNameIter = suppressedCompetitorNames.iterator(); suppressedCompetitorNameIter
                    .hasNext();) {
                String next = suppressedCompetitorNameIter.next();
                if (competitorsByName.containsKey(next)) {
                    leaderboard.setSuppressed(competitorsByName.get(next), true);
                    suppressedCompetitorNameIter.remove();
                }
            }
        }
        removeAsListenerIfNoLeftOvers();
    }

    private void removeAsListenerIfNoLeftOvers() {
        if (carriedPointsByCompetitorName.isEmpty() && maxPointsReasonsByCompetitorName.isEmpty() &&
                correctedScoresByCompetitorName.isEmpty() && displayNamesByCompetitorName.isEmpty() &&
                suppressedCompetitorNames.isEmpty() &&
            carriedPointsByCompetitorID.isEmpty() && maxPointsReasonsByCompetitorID.isEmpty() &&
                correctedScoresByCompetitorID.isEmpty() && displayNamesByCompetitorID.isEmpty() &&
                suppressedCompetitorIDs.isEmpty()) {
            getLeaderboard().removeRaceColumnListener(this);
            synchronized (listeners) {
                for (LeaderboardCorrectionsResolvedListener listener : listeners) {
                    listener.correctionsResolved(this);
                }
            }
        }
    }

    @Override
    public void setDisplayNameByID(Serializable competitorId, String displayName) {
        assertNoTrackedRaceAssociatedYet();
        Competitor competitor = competitorFactory.getExistingCompetitorById(competitorId);
        if (competitor != null) {
            leaderboard.setDisplayName(competitor, displayName);
        } else {
            synchronized (displayNamesByCompetitorID) {
                displayNamesByCompetitorID.put(competitorId, displayName);
            }
        }
    }

    @Override
    public void setDisplayNameByName(String competitorName, String displayName) {
        assertNoTrackedRaceAssociatedYet();
        synchronized (displayNamesByCompetitorName) {
            displayNamesByCompetitorName.put(competitorName, displayName);
        }
    }

    @Override
    public void suppressCompetitorById(Serializable competitorId) {
        assertNoTrackedRaceAssociatedYet();
        Competitor competitor = competitorFactory.getExistingCompetitorById(competitorId);
        if (competitor != null) {
            leaderboard.setSuppressed(competitor, true);
        } else {
            synchronized (suppressedCompetitorIDs) {
                suppressedCompetitorIDs.add(competitorId);
            }
        }
    }

    @Override
    public void suppressCompetitorByName(String competitorName) {
        assertNoTrackedRaceAssociatedYet();
        synchronized (suppressedCompetitorNames) {
            suppressedCompetitorNames.add(competitorName);
        }
    }

    @Override
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        assignLeftOvers(trackedRace);
    }

    @Override
    public void trackedRaceUnlinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
    }

    @Override
    public void isMedalRaceChanged(RaceColumn raceColumn, boolean newIsMedalRace) {
    }

    @Override
    public void isStartsWithZeroScoreChanged(RaceColumn raceColumn, boolean newIsStartsWithZeroScore) {
    }

    @Override
    public void hasSplitFleetContiguousScoringChanged(RaceColumn raceColumn, boolean hasSplitFleetContiguousScoring) {
    }

    @Override
    public void isFirstColumnIsNonDiscardableCarryForwardChanged(RaceColumn raceColumn, boolean firstColumnIsNonDiscardableCarryForward) {
    }

    @Override
    public boolean canAddRaceColumnToContainer(RaceColumn raceColumn) {
        return true;
    }

    @Override
    public void raceColumnAddedToContainer(RaceColumn raceColumn) {
    }

    @Override
    public void raceColumnRemovedFromContainer(RaceColumn raceColumn) {
    }

    @Override
    public void raceColumnMoved(RaceColumn raceColumn, int newIndex) {
    }

    @Override
    public void factorChanged(RaceColumn raceColumn, Double oldFactor, Double newFactor) {
    }

    @Override
    public void competitorDisplayNameChanged(Competitor competitor, String oldDisplayName, String displayName) {
    }

    @Override
    public void resultDiscardingRuleChanged(ResultDiscardingRule oldDiscardingRule, ResultDiscardingRule newDiscardingRule) {
    }

    @Override
    public void raceLogEventAdded(RaceColumn raceColumn, RaceLogIdentifier raceLogIdentifier, RaceLogEvent event) {
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public IsManagedByCache<SharedDomainFactory> resolve(SharedDomainFactory domainFactory) {
        this.competitorFactory = domainFactory;
        return this;
    }

}
