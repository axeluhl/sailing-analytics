package com.sap.sailing.domain.leaderboard.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.RaceColumnListenerWithDefaultAction;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.DelayedLeaderboardCorrections;
import com.sap.sailing.domain.leaderboard.Leaderboard;
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
public class DelayedLeaderboardCorrectionsImpl implements RaceColumnListenerWithDefaultAction, DelayedLeaderboardCorrections, IsManagedByCache<SharedDomainFactory> {
    private static final long serialVersionUID = 8824782847677232275L;
    
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
        removeAsListenerIfNoLeftOvers();
    }

    private void removeAsListenerIfNoLeftOvers() {
        if (carriedPointsByCompetitorID.isEmpty() && maxPointsReasonsByCompetitorID.isEmpty() &&
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
    public void suppressCompetitorByID(Serializable competitorId) {
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
    public void trackedRaceLinked(RaceColumn raceColumn, Fleet fleet, TrackedRace trackedRace) {
        assignLeftOvers(trackedRace);
    }

    @Override
    public IsManagedByCache<SharedDomainFactory> resolve(SharedDomainFactory domainFactory) {
        this.competitorFactory = domainFactory;
        return this;
    }

    /**
     * The default action for {@link RaceColumnListener} callbacks is to do nothing
     */
    @Override
    public void defaultAction() {
    }

}
