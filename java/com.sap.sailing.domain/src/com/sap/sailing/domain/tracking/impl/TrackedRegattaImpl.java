package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
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
import com.sap.sse.common.Util;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

public class TrackedRegattaImpl implements TrackedRegatta {
    private static final long serialVersionUID = 6480508193567014285L;

    private static final Logger logger = Logger.getLogger(TrackedRegattaImpl.class.getName());
    
    private final Regatta regatta;
    
    /**
     * Guards access to {@link #trackedRaces}. Callers of {@link #getTrackedRaces()} need to acquire the
     * read lock before iterating.
     */
    private final NamedReentrantReadWriteLock trackedRacesLock;
    
    /**
     * Guarded by {@link #trackedRacesLock}
     */
    private final Map<RaceDefinition, TrackedRace> trackedRaces;
    
    private final Map<BoatClass, Collection<TrackedRace>> trackedRacesByBoatClass;
    private transient Set<RaceListener> raceListeners;

    public TrackedRegattaImpl(Regatta regatta) {
        super();
        trackedRacesLock = new NamedReentrantReadWriteLock("trackeRaces lock for tracked regatta "+regatta.getName(), /* fair */ false);
        this.regatta = regatta;
        this.trackedRaces = new HashMap<RaceDefinition, TrackedRace>();
        this.trackedRacesByBoatClass = new HashMap<BoatClass, Collection<TrackedRace>>();
        raceListeners = new HashSet<RaceListener>();
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.raceListeners = new HashSet<RaceListener>();
    }
    
    @Override
    public void lockTrackedRacesForRead() {
        LockUtil.lockForRead(trackedRacesLock);
    }

    @Override
    public void unlockTrackedRacesAfterRead() {
        LockUtil.unlockAfterRead(trackedRacesLock);
    }

    @Override
    public void lockTrackedRacesForWrite() {
        LockUtil.lockForWrite(trackedRacesLock);
    }

    @Override
    public void unlockTrackedRacesAfterWrite() {
        LockUtil.unlockAfterWrite(trackedRacesLock);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        lockTrackedRacesForRead();
        try {
            oos.defaultWriteObject();
        } finally {
            unlockTrackedRacesAfterRead();
        }
    }
    
    /**
     * Resolving replaces this de-serialized object (which has a <code>null</code> {@link #raceListeners} collection) by
     * a new one into which all other collection contents are copied.
     */
    private Object readResolve() throws ObjectStreamException {
        TrackedRegattaImpl result = new TrackedRegattaImpl(this.regatta);
        result.trackedRaces.putAll(this.trackedRaces);
        result.trackedRacesByBoatClass.putAll(this.trackedRacesByBoatClass);
        return result;
    }

    @Override
    public void addTrackedRace(TrackedRace trackedRace) {
        final TrackedRace oldTrackedRace;
        LockUtil.lockForWrite(trackedRacesLock);
        try {
            logger.info("adding tracked race for "+trackedRace.getRace()+" to tracked regatta "+getRegatta().getName()+
                    " with regatta hash code "+getRegatta().hashCode());
            oldTrackedRace = trackedRaces.put(trackedRace.getRace(), trackedRace);
            if (oldTrackedRace != trackedRace) {
                Collection<TrackedRace> coll = trackedRacesByBoatClass.get(trackedRace.getRace().getBoatClass());
                if (coll == null) {
                    coll = new ArrayList<TrackedRace>();
                    trackedRacesByBoatClass.put(trackedRace.getRace().getBoatClass(), coll);
                }
                coll.add(trackedRace);
            }
        } finally {
            LockUtil.unlockAfterWrite(trackedRacesLock);
        }
        if (oldTrackedRace != trackedRace) {
            for (RaceListener listener : raceListeners) {
                listener.raceAdded(trackedRace);
            }
        }
    }
    
    @Override
    public void removeTrackedRace(RaceDefinition raceDefinition) {
        LockUtil.lockForWrite(trackedRacesLock);
        try {
            trackedRaces.remove(raceDefinition);
        } finally {
            LockUtil.unlockAfterWrite(trackedRacesLock);
        }
    }
    
    @Override
    public void removeTrackedRace(TrackedRace trackedRace) {
        LockUtil.lockForWrite(trackedRacesLock);
        try {
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
        } finally {
            LockUtil.unlockAfterWrite(trackedRacesLock);
        }
    }

    @Override
    public Regatta getRegatta() {
        return regatta;
    }

    @Override
    public Iterable<? extends TrackedRace> getTrackedRaces() {
        if (trackedRacesLock.getReadHoldCount() <= 0 && trackedRacesLock.getWriteHoldCount() <= 0) {
            throw new IllegalStateException("Callers of TrackedRegatta.getTrackedRaces() must hold the read lock; see TrackedRegatta.lockTrackedRacesForRead()");
        }
        return trackedRaces.values();
    }

    @Override
    public Iterable<TrackedRace> getTrackedRaces(BoatClass boatClass) {
        return trackedRacesByBoatClass.get(boatClass);
    }

    @Override
    public TrackedRace getTrackedRace(RaceDefinition race) {
        boolean interrupted = false;
        lockTrackedRacesForRead();
        try {
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
        } finally {
            unlockTrackedRacesAfterRead();
        }
    }
    
    @Override
    public TrackedRace getExistingTrackedRace(RaceDefinition race) {
        lockTrackedRacesForRead();
        try {
            return trackedRaces.get(race);
        } finally {
            unlockTrackedRacesAfterRead();
        }
    }

    @Override
    public void addRaceListener(RaceListener listener) {
        final List<TrackedRace> trackedRacesCopy = new ArrayList<>();
        lockTrackedRacesForRead();
        try {
            raceListeners.add(listener);
            Util.addAll(getTrackedRaces(), trackedRacesCopy);
        } finally {
            unlockTrackedRacesAfterRead();
        }
        for (TrackedRace trackedRace : trackedRacesCopy) {
            listener.raceAdded(trackedRace);
        }
    }

    @Override
    public int getNetPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        int result = 0;
        lockTrackedRacesForRead();
        try {
            for (TrackedRace trackedRace : getTrackedRaces()) {
                result += trackedRace.getRank(competitor, timePoint);
            }
            return result;
        } finally {
            unlockTrackedRacesAfterRead();
        }
    }

    @Override
    public DynamicTrackedRace createTrackedRace(RaceDefinition raceDefinition, Iterable<Sideline> sidelines,
            WindStore windStore, GPSFixStore gpsFixStore, long delayToLiveInMillis,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, boolean useInternalMarkPassingAlgorithm, RaceLogResolver raceLogResolver) {
        logger.log(Level.INFO, "Creating DynamicTrackedRaceImpl for RaceDefinition " + raceDefinition.getName());
        DynamicTrackedRaceImpl result = new DynamicTrackedRaceImpl(this, raceDefinition, sidelines, windStore,
                gpsFixStore, delayToLiveInMillis, millisecondsOverWhichToAverageWind,
                millisecondsOverWhichToAverageSpeed,
                /* useMarkPassingCalculator */useInternalMarkPassingAlgorithm, getRegatta().getRankingMetricConstructor(), raceLogResolver);
        // adding the raceDefinition to the raceDefinitionSetToUpdate BEFORE calling addTrackedRace helps those who
        // are called back by RaceListener.raceAdded(TrackedRace) and who then expect the update to have happened
        if (raceDefinitionSetToUpdate != null) {
            raceDefinitionSetToUpdate.addRaceDefinition(raceDefinition, result);
        }
        addTrackedRace(result);
        return result;
    }
}
