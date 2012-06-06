package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedRace;

public abstract class AbstractRaceColumn implements RaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;
    
    private final Map<Fleet, TrackedRace> trackedRaces;
    private String name;
    private final Map<Fleet, RaceIdentifier> raceIdentifiers;
    private transient Set<RaceColumnListener> listeners;
    
    public AbstractRaceColumn(String name) {
        this.name = name;
        this.trackedRaces = new HashMap<Fleet, TrackedRace>();
        this.raceIdentifiers = new HashMap<Fleet, RaceIdentifier>();
        this.listeners = new HashSet<RaceColumnListener>();
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        listeners = new HashSet<RaceColumnListener>();
    }
    
    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListenersAboutTrackedRaceLinked(Fleet fleet, TrackedRace trackedRace) {
        for (RaceColumnListener listener : listeners) {
            listener.trackedRaceLinked(this, fleet, trackedRace);
        }
    }

    private void notifyListenersAboutTrackedRaceUnlinked(Fleet fleet, TrackedRace trackedRace) {
        for (RaceColumnListener listener : listeners) {
            listener.trackedRaceUnlinked(this, fleet, trackedRace);
        }
    }

    @Override
    public TrackedRace getTrackedRace(Fleet fleet) {
        return trackedRaces.get(fleet);
    }

    @Override
    public void setTrackedRace(Fleet fleet, TrackedRace trackedRace) {
        TrackedRace previouslyLinkedRace = this.trackedRaces.put(fleet, trackedRace);
        this.setRaceIdentifier(fleet, trackedRace == null ? null : trackedRace.getRaceIdentifier());
        if (previouslyLinkedRace != null) {
            notifyListenersAboutTrackedRaceUnlinked(fleet, previouslyLinkedRace);
        }
        if (trackedRace != null) {
            notifyListenersAboutTrackedRaceLinked(fleet, trackedRace);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    @Override
    public Pair<Competitor, RaceColumn> getKey(Competitor competitor) {
        return new Pair<Competitor, RaceColumn>(competitor, this);
    }

    @Override
    public RaceIdentifier getRaceIdentifier(Fleet fleet) {
        return raceIdentifiers.get(fleet);
    }

    @Override
    public void setRaceIdentifier(Fleet fleet, RaceIdentifier raceIdentifier) {
        this.raceIdentifiers.put(fleet, raceIdentifier);
    }

    @Override
    public void releaseTrackedRace(Fleet fleet) {
        trackedRaces.remove(fleet);
    }

    @Override
    public Fleet getFleetOfCompetitor(Competitor competitor) {
        for (Map.Entry<Fleet, TrackedRace> e : trackedRaces.entrySet()) {
            if (Util.contains(e.getValue().getRace().getCompetitors(), competitor)) {
                return e.getKey();
            }
        }
        return null;
    }

    @Override
    public Fleet getFleetByName(String fleetName) {
        for (Fleet fleet : getFleets()) {
            if (fleet.getName().equals(fleetName)) {
                return fleet;
            }
        }
        return null;
    }

    @Override
    public boolean hasTrackedRaces() {
        return !trackedRaces.isEmpty();
    }

    @Override
    public TrackedRace getTrackedRace(Competitor competitor) {
        for (TrackedRace trackedRace : trackedRaces.values()) {
            if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
                return trackedRace;
            }
        }
        return null;
    }
}
