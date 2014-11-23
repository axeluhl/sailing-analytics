package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.DynamicRaceDefinitionSet;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sse.common.TimePoint;

public class TrackedRegattaImpl implements TrackedRegatta {
    private static final long serialVersionUID = 6480508193567014285L;

    private static final Logger logger = Logger.getLogger(TrackedRegattaImpl.class.getName());
    
    private final Regatta regatta;
    private final Map<RaceDefinition, TrackedRace> trackedRaces;
    private final Map<BoatClass, Collection<TrackedRace>> trackedRacesByBoatClass;
    private transient Set<RaceListener> raceListeners;

    public TrackedRegattaImpl(Regatta regatta) {
        super();
        this.regatta = regatta;
        this.trackedRaces = new HashMap<RaceDefinition, TrackedRace>();
        this.trackedRacesByBoatClass = new HashMap<BoatClass, Collection<TrackedRace>>();
        raceListeners = new HashSet<RaceListener>();
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.raceListeners = new HashSet<RaceListener>();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        synchronized (trackedRaces) {
            oos.defaultWriteObject();
        }
    }
    /**
     * Resolving replaces this de-serialized object (which has a <code>null</code> {@link #raceListeners} collection) by
     * a new one into which all other collection contents are copied.
     */
    private Object readResolve() {
        TrackedRegattaImpl result = new TrackedRegattaImpl(this.regatta);
        result.trackedRaces.putAll(this.trackedRaces);
        result.trackedRacesByBoatClass.putAll(this.trackedRacesByBoatClass);
        return result;
    }

    @Override
    public void addTrackedRace(TrackedRace trackedRace) {
        synchronized (trackedRaces) {
            logger.info("adding tracked race for "+trackedRace.getRace()+" to tracked regatta "+getRegatta().getName()+
                    " with regatta hash code "+getRegatta().hashCode());
            TrackedRace oldTrackedRace = trackedRaces.put(trackedRace.getRace(), trackedRace);
            if (oldTrackedRace != trackedRace) {
                Collection<TrackedRace> coll = trackedRacesByBoatClass.get(trackedRace.getRace().getBoatClass());
                if (coll == null) {
                    coll = new ArrayList<TrackedRace>();
                    trackedRacesByBoatClass.put(trackedRace.getRace().getBoatClass(), coll);
                }
                coll.add(trackedRace);
                for (RaceListener listener : raceListeners) {
                    listener.raceAdded(trackedRace);
                }
                trackedRaces.notifyAll();
            }
        }
    }
    
    @Override
    public void removeTrackedRace(RaceDefinition raceDefinition) {
        synchronized (trackedRaces) {
            trackedRaces.remove(raceDefinition);
        }
    }
    
    @Override
    public void removeTrackedRace(TrackedRace trackedRace) {
        synchronized (trackedRaces) {
            trackedRaces.remove(trackedRace.getRace());
            Collection<TrackedRace> trbbc = trackedRacesByBoatClass.get(trackedRace.getRace().getBoatClass());
            if (trbbc != null) {
                trbbc.remove(trackedRace);
                if (trbbc.isEmpty()) {
                    trackedRacesByBoatClass.remove(trackedRace.getRace().getBoatClass());
                }
            }
            for (RaceListener listener : raceListeners) {
                listener.raceRemoved(trackedRace);
            }
            trackedRaces.notifyAll();
        }
    }

    @Override
    public Regatta getRegatta() {
        return regatta;
    }

    @Override
    public Iterable<? extends TrackedRace> getTrackedRaces() {
        return trackedRaces.values();
    }

    @Override
    public Iterable<TrackedRace> getTrackedRaces(BoatClass boatClass) {
        return trackedRacesByBoatClass.get(boatClass);
    }

    @Override
    public TrackedRace getTrackedRace(RaceDefinition race) {
        boolean interrupted = false;
        synchronized (trackedRaces) {
            TrackedRace result = trackedRaces.get(race);
            while (!interrupted && result == null) {
                try {
                    trackedRaces.wait();
                    result = trackedRaces.get(race);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            return result;
        }
    }
    
    @Override
    public TrackedRace getExistingTrackedRace(RaceDefinition race) {
        return trackedRaces.get(race);
    }

    @Override
    public void addRaceListener(RaceListener listener) {
        raceListeners.add(listener);
        synchronized (trackedRaces) {
            for (TrackedRace trackedRace : getTrackedRaces()) {
                listener.raceAdded(trackedRace);
            }
        }
    }

    @Override
    public int getNetPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        int result = 0;
        for (TrackedRace trackedRace : getTrackedRaces()) {
            result += trackedRace.getRank(competitor, timePoint);
        }
        return result;
    }

    @Override
    public DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, Iterable<Sideline> sidelines, WindStore windStore, GPSFixStore gpsFixStore, long delayToLiveInMillis,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate) {
        logger.log(Level.INFO, "Creating DynamicTrackedRaceImpl for RaceDefinition " + raceDefinition.getName());
        DynamicTrackedRaceImpl result = new DynamicTrackedRaceImpl(this, raceDefinition, sidelines,
                windStore, gpsFixStore, delayToLiveInMillis, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed);
        // adding the raceDefinition to the raceDefinitionSetToUpdate BEFORE calling addTrackedRace helps those who
        // are called back by RaceListener.raceAdded(TrackedRace) and who then expect the update to have happened
        if (raceDefinitionSetToUpdate != null) {
            raceDefinitionSetToUpdate.addRaceDefinition(raceDefinition, result);
        }
        addTrackedRace(result);
        return result;
    }

}
