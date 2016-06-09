package com.sap.sailing.domain.racelogtracking.impl.logtracker;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RegattaLogAttachmentListener;
import com.sap.sailing.domain.tracking.TrackingDataLoader;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sailing.domain.tracking.impl.TrackedRaceStatusImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

public abstract class AbstractRaceLogFixTracker implements TrackingDataLoader {
    private static final Logger logger = Logger.getLogger(AbstractRaceLogFixTracker.class.getName());

    protected final DynamicTrackedRace trackedRace;
    
    private final Set<RegattaLog> knownRegattaLogs = new HashSet<>();
    
    private final NamedReentrantReadWriteLock loadingFromFixStoreLock;

    // TODO: move to AbstractRaceLogFixTracker
   private final RegattaLogAttachmentListener regattaLogAttachmentListener = new RegattaLogAttachmentListener() {
        @Override
        public void regattaLogAboutToBeAttached(RegattaLog regattaLog) {
            synchronized (knownRegattaLogs) {
                addRegattaLogUnlocked(regattaLog);
            }
            updateMappingsAndAddListeners();
        }

        @Override
        public void onStopTracking(boolean preemptive) {
            if (preemptive) {
                waitForLoadingFromFixStoreToFinishRunning();
            }
            stop();
        }
    };
    private final AbstractRaceChangeListener trackingTimesRaceChangeListener = new AbstractRaceChangeListener() {
        
        @Override
        public void startOfTrackingChanged(TimePoint oldStartOfTracking, TimePoint newStartOfTracking) {
            if ((newStartOfTracking == null || (oldStartOfTracking != null && newStartOfTracking
                    .before(oldStartOfTracking)))) {
                loadFixesForExtendedTimeRange(newStartOfTracking, oldStartOfTracking);
            }
        }
        
        @Override
        public void endOfTrackingChanged(TimePoint oldEndOfTracking, TimePoint newEndOfTracking) {
            if (newEndOfTracking == null || (oldEndOfTracking != null && newEndOfTracking.after(oldEndOfTracking))) {
                loadFixesForExtendedTimeRange(oldEndOfTracking, newEndOfTracking);
            }
        }
    };

    public AbstractRaceLogFixTracker(DynamicTrackedRace trackedRace, String fixLoadingLockName) {
        this.trackedRace = trackedRace;

        loadingFromFixStoreLock = new NamedReentrantReadWriteLock(fixLoadingLockName, false);
    }

    protected void waitForLoadingFromFixStoreToFinishRunning() {
        try {
            waitForLoadingFromGPSFixStoreToFinishRunning();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted while waiting for Fixes to be loaded", e);
        }
    }

    // TEMP: kommt von RaceLogSensorFixTracker
    protected TimePoint getStartOfTracking() {
        return trackedRace.getStartOfTracking();
    }

    // TEMP: kommt von RaceLogSensorFixTracker
    protected TimePoint getEndOfTracking() {
        return trackedRace.getEndOfTracking();
    }

    private void addRegattaLogUnlocked(RegattaLog log) {
        log.addListener(getRegattaLogEventVisitor());
        knownRegattaLogs.add(log);
    }
    
    protected void forEachRegattaLog(Consumer<RegattaLog> regattaLogConsumer) {
        synchronized (knownRegattaLogs) {
            knownRegattaLogs.forEach(regattaLogConsumer);
        }
    }
    
    protected abstract void loadFixesForExtendedTimeRange(TimePoint loadFixesFrom, TimePoint loadFixesTo);
    
    protected abstract RegattaLogEventVisitor getRegattaLogEventVisitor();
    
    protected void startTracking() {
        trackedRace.addRegattaLogAttachmentListener(regattaLogAttachmentListener);
        final boolean hasRegattaLogs;
        synchronized (knownRegattaLogs) {
            trackedRace.getAttachedRegattaLogs().forEach(this::addRegattaLogUnlocked);
            hasRegattaLogs = !knownRegattaLogs.isEmpty();
        }
        trackedRace.addListener(trackingTimesRaceChangeListener);
        if (hasRegattaLogs) {
            updateMappingsAndAddListeners();
            waitForLoadingFromFixStoreToFinishRunning();
        }
    }
    
    protected abstract void updateMappingsAndAddListenersImpl();
    
    public void stop() {
        stopTracking();
    }

    protected void stopTracking() {
        trackedRace.removeListener(trackingTimesRaceChangeListener);
        trackedRace.removeRegattaLogAttachmentListener(regattaLogAttachmentListener);
        synchronized (knownRegattaLogs) {
            knownRegattaLogs.forEach((log) -> log.removeListener(getRegattaLogEventVisitor()));
            knownRegattaLogs.clear();
        }
        setStatusAndProgress(TrackedRaceStatusEnum.FINISHED, 1.0);
    }
    
    private final AtomicInteger activeLoaders = new AtomicInteger();
    
    protected void updateMappingsAndAddListeners() {
        synchronized (AbstractRaceLogFixTracker.this) {
            activeLoaders.incrementAndGet();
            setStatusAndProgress(TrackedRaceStatusEnum.LOADING, 0.5);
        }
        
        Thread t = new Thread(this.getClass().getSimpleName() + " loader for tracked race " + trackedRace.getRace().getName()) {
            @Override
            public void run() {
                trackedRace.lockForSerializationRead();
                setStatusAndProgress(TrackedRaceStatusEnum.LOADING, 0.5);
                LockUtil.lockForWrite(loadingFromFixStoreLock);
                synchronized (AbstractRaceLogFixTracker.this) {
                    AbstractRaceLogFixTracker.this.notifyAll();
                }
                
                try {
                    updateMappingsAndAddListenersImpl();
                } finally {
                    LockUtil.unlockAfterWrite(loadingFromFixStoreLock);
                    synchronized (AbstractRaceLogFixTracker.this) {
                        int currentActiveLoaders;
                        currentActiveLoaders = activeLoaders.decrementAndGet();
                        AbstractRaceLogFixTracker.this.notifyAll();
                        if(currentActiveLoaders == 0) {
                            setStatusAndProgress(TrackedRaceStatusEnum.TRACKING, 1.0);
                        }
                    }
                    trackedRace.unlockAfterSerializationRead();
                    logger.info("Thread "+getName()+" done.");
                }
            }
        };
        t.start();
//        if (waitForGPSFixesToLoad) {
//            try {
//                t.join();
//            } catch (InterruptedException e) {
//                logger.log(Level.WARNING, "Got interrupted while waiting for loading of GPS fixes from log "+log+" to finish", e);
//            }
//        }
    }
    
    private synchronized void waitForLoadingFromGPSFixStoreToFinishRunning() throws InterruptedException {
        while (activeLoaders.get() > 0) {
            wait();
        }
    }
    
    /**
     * Tells if currently the race is loading GPS fixes from the {@link GPSFixStore}. Clients may {@link Object#wait()} on <code>this</code>
     * object and will be notified whenever a change of this flag's value occurs.
     */
    public boolean isLoadingFromGPSFixStore() {
        return activeLoaders.get() > 0;
    }
    
    private void setStatusAndProgress(TrackedRaceStatusEnum status, double progress) {
        trackedRace.onStatusChanged(this, new TrackedRaceStatusImpl(status, progress));
    }
}
