package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifierTemplate;
import com.sap.sailing.domain.racelog.RaceLogInformation;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.tracking.TrackedRace;

public abstract class AbstractRaceColumn extends SimpleAbstractRaceColumn implements RaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;
    
    private final Map<Fleet, TrackedRace> trackedRaces;
    private final Map<Fleet, RaceIdentifier> raceIdentifiers;
    
    private final Map<Fleet, RaceLog> raceLogs;
    private RaceLogIdentifierTemplate raceLogsIdentifier;
    
    public AbstractRaceColumn() {
        this.trackedRaces = new HashMap<Fleet, TrackedRace>();
        this.raceIdentifiers = new HashMap<Fleet, RaceIdentifier>();
        this.raceLogs = new HashMap<Fleet, RaceLog>();
    }
    
    @Override
    public synchronized void setRaceLogInformation(final RaceLogInformation information) {
        raceLogs.clear();
        raceLogsIdentifier = information.getIdentifierTemplate();
        RaceLogStore store = information.getStore();
        for (final Fleet fleet : getFleets()) {
            RaceLogIdentifier identifier = raceLogsIdentifier.compile(fleet);
            RaceLog raceLog = store.getRaceLog(identifier);
            raceLog.addListener(new RaceColumnRaceLogReplicator(this, identifier));
            raceLogs.put(fleet, raceLog);
        }
    }
    
    @Override
    public RaceLog getRaceLog(Fleet fleet) {
    	return raceLogs.get(fleet);
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
                previouslyLinkedRace.detachRaceLog();
                getRaceColumnListeners().notifyListenersAboutTrackedRaceUnlinked(this, fleet, previouslyLinkedRace);
            }
            if (trackedRace != null) {
                trackedRace.attachRaceLog(getRaceLog(fleet));
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
    
    /**
     * When deserializing, replication listeners are registered on all race logs.
     */
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        
        for (Entry<Fleet, RaceLog> entry : raceLogs.entrySet()) {
            Fleet fleet = entry.getKey();
            RaceLog raceLog = entry.getValue();
            raceLog.addListener(new RaceColumnRaceLogReplicator(this, raceLogsIdentifier.compile(fleet)));
        }
    }
}
