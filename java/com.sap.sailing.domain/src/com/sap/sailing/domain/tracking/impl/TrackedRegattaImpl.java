package com.sap.sailing.domain.tracking.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Sideline;
import com.sap.sailing.domain.common.NoWindException;
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
    
    private transient ConcurrentMap<RaceListener, RaceListener> raceListeners;
    
    private transient WorkQueue eventQueue;

    public TrackedRegattaImpl(Regatta regatta) {
        super();
        trackedRacesLock = new NamedReentrantReadWriteLock("trackeRaces lock for tracked regatta "+regatta.getName(), /* fair */ false);
        this.regatta = regatta;
        this.trackedRaces = new HashMap<RaceDefinition, TrackedRace>();
        raceListeners = new ConcurrentHashMap<RaceListener, RaceListener>();
        eventQueue = new WorkQueue();
    }
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        this.raceListeners = new ConcurrentHashMap<RaceListener, RaceListener>();
        eventQueue = new WorkQueue();
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
        return result;
    }

    @Override
    public void addTrackedRace(TrackedRace trackedRace) {
        final TrackedRace oldTrackedRace;
        lockTrackedRacesForWrite();
        try {
            logger.info("adding tracked race for "+trackedRace.getRace()+" to tracked regatta "+getRegatta().getName()+
                    " with regatta hash code "+getRegatta().hashCode());
            oldTrackedRace = trackedRaces.put(trackedRace.getRace(), trackedRace);
            if (oldTrackedRace != trackedRace) {
                notifyListenersAboutTrackedRaceAdded(trackedRace);
            }
        } finally {
            unlockTrackedRacesAfterWrite();
        }
    }

    protected void notifyListenersAboutTrackedRaceAdded(TrackedRace trackedRace) {
        enqueEvent(listener -> listener.raceAdded(trackedRace));
    }
    
    protected void enqueEvent(Consumer<RaceListener> fireEventCallback) {
        final Set<RaceListener> listenersToInform = new HashSet<>(raceListeners.keySet());
        eventQueue.addWork(() -> {
            for (RaceListener listener : listenersToInform) {
                fireEventCallback.accept(listener);
            }
        });
    }
    
    @Override
    public void removeTrackedRace(RaceDefinition raceDefinition) {
        lockTrackedRacesForWrite();
        try {
            trackedRaces.remove(raceDefinition);
        } finally {
            unlockTrackedRacesAfterWrite();
        }
    }
    
    @Override
    public void removeTrackedRace(TrackedRace trackedRace) {
        lockTrackedRacesForWrite();
        try {
            trackedRaces.remove(trackedRace.getRace());
            notifyListenersAboutTrackedRaceRemoved(trackedRace);
        } finally {
            unlockTrackedRacesAfterWrite();
        }
    }

    protected void notifyListenersAboutTrackedRaceRemoved(TrackedRace trackedRace) {
        enqueEvent(listener -> listener.raceRemoved(trackedRace));
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
    public TrackedRace getTrackedRace(RaceDefinition race) {
        boolean interrupted = false;
        TrackedRace result = getExistingTrackedRace(race);
        if (!interrupted && result == null) {
            final Object mutex = new Object();
            final RaceListener listener = new RaceListener() {
                @Override
                public void raceRemoved(TrackedRace trackedRace) {}
                
                @Override
                public void raceAdded(TrackedRace trackedRace) {
                    synchronized (mutex) {
                        mutex.notifyAll();
                    }
                }
            };
            addRaceListener(listener);
            try {
                synchronized (mutex) {
                    result = getExistingTrackedRace(race);
                    while (!interrupted && result == null) {
                        try {
                            mutex.wait();
                            result = getExistingTrackedRace(race);
                        } catch (InterruptedException e) {
                            interrupted = true;
                        }
                    }
                }
            } finally {
                removeRaceListener(listener);
            }
        }
        return result;
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
        lockTrackedRacesForRead();
        try {
            raceListeners.put(listener, listener);
            final List<TrackedRace> trackedRacesCopy = new ArrayList<>();
            Util.addAll(getTrackedRaces(), trackedRacesCopy);
            eventQueue.addWork(() -> {
                for (TrackedRace trackedRace : trackedRacesCopy) {
                    listener.raceAdded(trackedRace);
                }
            });
        } finally {
            unlockTrackedRacesAfterRead();
        }
    }

    @Override
    public Future<Boolean> removeRaceListener(RaceListener listener) {
        final CompletableFuture<Boolean> result = new CompletableFuture<Boolean>();
        lockTrackedRacesForRead();
        try {
            if (raceListeners.remove(listener) != null) {
                eventQueue.addWork(() -> {
                    result.complete(Boolean.TRUE);
                });
            } else {
                result.complete(Boolean.TRUE);
            }
        } finally {
            unlockTrackedRacesAfterRead();
        }
        return result;
    }

    @Override
    public int getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
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
            WindStore windStore, long delayToLiveInMillis,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            DynamicRaceDefinitionSet raceDefinitionSetToUpdate, boolean useInternalMarkPassingAlgorithm, RaceLogResolver raceLogResolver) {
        logger.log(Level.INFO, "Creating DynamicTrackedRaceImpl for RaceDefinition " + raceDefinition.getName());
        DynamicTrackedRaceImpl result = new DynamicTrackedRaceImpl(this, raceDefinition, sidelines, windStore,
                delayToLiveInMillis, millisecondsOverWhichToAverageWind,
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
