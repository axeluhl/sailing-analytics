package com.sap.sailing.domain.base.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.tracking.TrackedRace;

public abstract class AbstractRaceColumn extends SimpleAbstractRaceColumn implements RaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;
    
    private final Map<Fleet, TrackedRace> trackedRaces;
    private final Map<Fleet, RaceIdentifier> raceIdentifiers;
    public AbstractRaceColumn() {
        this.trackedRaces = new HashMap<Fleet, TrackedRace>();
        this.raceIdentifiers = new HashMap<Fleet, RaceIdentifier>();
    }
    
    @Override
    public TrackedRace getTrackedRace(Fleet fleet) {
        return trackedRaces.get(fleet);
    }

    @Override
    public void setTrackedRace(Fleet fleet, TrackedRace trackedRace) {
        TrackedRace previouslyLinkedRace = this.trackedRaces.get(fleet);
        if (trackedRace != previouslyLinkedRace) {
            synchronized (this) {
                if (trackedRace == null) {
                    setRaceIdentifier(fleet, null);
                    this.trackedRaces.remove(fleet);
                } else {
                    this.trackedRaces.put(fleet, trackedRace);
                    this.setRaceIdentifier(fleet, trackedRace.getRaceIdentifier());
                }
            }
            if (previouslyLinkedRace != null) {
                getRaceColumnListeners().notifyListenersAboutTrackedRaceUnlinked(this, fleet, previouslyLinkedRace);
            }
            if (trackedRace != null) {
                getRaceColumnListeners().notifyListenersAboutTrackedRaceLinked(this, fleet, trackedRace);
            }
        }
    }

    @Override
    public RaceIdentifier getRaceIdentifier(Fleet fleet) {
        return raceIdentifiers.get(fleet);
    }

    @Override
    public void setRaceIdentifier(Fleet fleet, RaceIdentifier raceIdentifier) {
        if (raceIdentifier == null) {
            this.raceIdentifiers.remove(fleet);
        } else {
            this.raceIdentifiers.put(fleet, raceIdentifier);
        }
    }

    @Override
    public synchronized void releaseTrackedRace(Fleet fleet) {
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
    
    @Override
    public String toString() {
        return getName();
    }
}
