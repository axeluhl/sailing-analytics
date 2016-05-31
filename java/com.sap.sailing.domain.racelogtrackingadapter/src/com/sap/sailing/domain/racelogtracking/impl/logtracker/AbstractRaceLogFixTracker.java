package com.sap.sailing.domain.racelogtracking.impl.logtracker;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLogEventVisitor;
import com.sap.sailing.domain.racelogsensortracking.impl.FixLoadingTask;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RegattaLogAttachmentListener;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.TimePoint;

public abstract class AbstractRaceLogFixTracker {
    private static final Logger logger = Logger.getLogger(AbstractRaceLogFixTracker.class.getName());

    protected final DynamicTrackedRegatta trackedRegatta;
    protected final DynamicTrackedRace trackedRace;
    
    private final Set<RegattaLog> knownRegattaLogs = new HashSet<>();
    
    private final FixLoadingTask fixLoadingTask;

    // TODO: move to AbstractRaceLogFixTracker
   private final RegattaLogAttachmentListener regattaLogAttachmentListener = new RegattaLogAttachmentListener() {
        @Override
        public void regattaLogAboutToBeAttached(RegattaLog regattaLog) {
            synchronized (knownRegattaLogs) {
                addRegattaLogUnlocked(regattaLog);
            }
            updateMappingsAndAddListeners();
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

    public AbstractRaceLogFixTracker(DynamicTrackedRegatta trackedRegatta, DynamicTrackedRace trackedRace, String fixLoadingLockName) {
        this.trackedRegatta = trackedRegatta;
        this.trackedRace = trackedRace;

        this.fixLoadingTask = new FixLoadingTask(trackedRace, fixLoadingLockName);
    }

    protected void waitForLoadingFromFixStoreToFinishRunning() {
        try {
            fixLoadingTask.waitForLoadingFromGPSFixStoreToFinishRunning();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted while waiting for Fixes to be loaded", e);
        }
    }

    public final DynamicTrackedRegatta getTrackedRegatta() {
        return trackedRegatta;
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

    protected void updateMappingsAndAddListeners() {
        fixLoadingTask.loadFixesForLog(this::updateMappingsAndAddListenersImpl,
                "Mongo sensor track loader for tracked race " + trackedRace.getRace().getName());
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
    }
}
