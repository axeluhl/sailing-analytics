package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.racelog.RaceLogIdentifier;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.impl.RaceLogIdentifierImpl;
import com.sap.sailing.domain.regattalike.RegattaLikeIdentifier;
import com.sap.sailing.domain.tracking.RaceExecutionOrderProvider;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;

public abstract class AbstractRaceColumn extends SimpleAbstractRaceColumn implements RaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;

    private TrackedRaces trackedRaces;
    private Map<Fleet, RaceIdentifier> raceIdentifiers;

    private Map<Fleet, RaceLog> raceLogs;
    
    private transient RaceLogStore raceLogStore;
    private RegattaLikeIdentifier regattaLikeParent;

    public AbstractRaceColumn() {
        this.trackedRaces = new TrackedRaces();
        this.raceIdentifiers = new HashMap<Fleet, RaceIdentifier>();
        this.raceLogs = new HashMap<Fleet, RaceLog>();
    }

    @Override
    public synchronized void setRaceLogInformation(RaceLogStore raceLogStore, RegattaLikeIdentifier regattaLikeParent) {
        this.raceLogStore = raceLogStore;
        this.regattaLikeParent = regattaLikeParent;
        for (final Fleet fleet : getFleets()) {
            reloadRaceLog(fleet);
        }
    }

    @Override
    public RaceLog getRaceLog(Fleet fleet) {
        synchronized (raceLogs) {
            return raceLogs.get(fleet);
        }
    }
    
    @Override
    public RaceLogIdentifier getRaceLogIdentifier(Fleet fleet) {
        return new RaceLogIdentifierImpl(regattaLikeParent, getName(), fleet);
    }

    @Override
    public TrackedRace getTrackedRace(Fleet fleet) {
        return trackedRaces.get(fleet);
    }

    @Override
    public void setTrackedRace(Fleet fleet, TrackedRace trackedRace) {
        TrackedRace previouslyLinkedRace = this.trackedRaces.get(fleet);
        if (trackedRace != previouslyLinkedRace) {
            releaseTrackedRace(fleet);
            synchronized (this) {
                if (trackedRace != null) {
                    this.trackedRaces.put(fleet, trackedRace);
                    this.setRaceIdentifier(fleet, trackedRace.getRaceIdentifier());
                }
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
        if (raceIdentifier != null) {
            this.raceIdentifiers.put(fleet, raceIdentifier);
        }
    }

    @Override
    public synchronized void releaseTrackedRace(Fleet fleet) {
        TrackedRace previouslyLinkedRace = this.trackedRaces.get(fleet);
        this.trackedRaces.remove(fleet);
        if (previouslyLinkedRace != null && regattaLikeParent != null) {
            RaceLogIdentifier identifier = getRaceLogIdentifier(fleet);
            previouslyLinkedRace.detachRaceLog(identifier.getIdentifier());
            getRaceColumnListeners().notifyListenersAboutTrackedRaceUnlinked(this, fleet, previouslyLinkedRace);
        }
    }
    
    @Override
    public void removeRaceIdentifier(Fleet fleet) {
        releaseTrackedRace(fleet);
        this.raceIdentifiers.remove(fleet);
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

    @Override
    public void reloadRaceLog(Fleet fleet) {
        synchronized(raceLogs) {
            RaceLogIdentifier identifier = getRaceLogIdentifier(fleet);
            RaceLog newOrLoadedRaceLog = raceLogStore.getRaceLog(identifier, /*ignoreCache*/ true);
            RaceLog raceLogAvailable = raceLogs.get(fleet);
            if (raceLogAvailable == null) {
                RaceColumnRaceLogReplicator listener = new RaceColumnRaceLogReplicator(this, identifier);
                newOrLoadedRaceLog.addListener(listener);
                raceLogs.put(fleet, newOrLoadedRaceLog);
            } else {
                // now add all race log events from newOrLoadedRaceLog that are not already in raceLogAvailable
                raceLogAvailable.merge(newOrLoadedRaceLog);
            }
        }
    }

    /**
     * When deserializing, replication listeners are registered on all race logs.
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        for (Entry<Fleet, RaceLog> entry : raceLogs.entrySet()) {
            Fleet fleet = entry.getKey();
            RaceLog raceLog = entry.getValue();
            raceLog.addListener(new RaceColumnRaceLogReplicator(this, getRaceLogIdentifier(fleet)));
            // now comes a little secrecy (see bug 2506) about how, after de-serialization, the connections
            // between race column, race log, tracked race and the listener pumping stuff from the race log
            // into the tracked race are re-established. The race log's listener structure is transient, and so
            // is the tracked race's attachedRaceLogs field and the logListener field. The collections and the
            // DynamicTrackedRaceLogListener are re-established after de-serialization in corresponding readObject(...)
            // methods. However, the connections are not. That's what we need to do here, simply by invoking:
            ois.registerValidation(() -> {
                TrackedRace trackedRace = getTrackedRace(fleet);
                if (trackedRace != null) {
                    trackedRace.attachRaceLog(raceLog);
                }
            }, /* prio */ 0);
            // because this will add the race log to the tracked race's attachedRaceLogs collection again, and
            // the new DynamicTrackedRaceLogListener that the readObject(...) method had constructed for the
            // tracked race will be added as a listener to the race log whose listeners collection otherwise would
            // only hold the RaceColumnRaceLogReplicator object added above.
        }
    }

    @Override
    public void setMasterDataExportOngoingThreadFlag(boolean flagValue) {
        trackedRaces.setMasterDataExportOngoingThreadFlag(flagValue);
    }
    
    public abstract RaceExecutionOrderProvider getRaceExecutionProvider();
}
