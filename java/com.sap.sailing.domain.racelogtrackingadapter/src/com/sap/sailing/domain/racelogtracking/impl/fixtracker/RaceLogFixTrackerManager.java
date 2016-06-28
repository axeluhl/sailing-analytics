package com.sap.sailing.domain.racelogtracking.impl.fixtracker;

import java.util.logging.Logger;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogRevokeEvent;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogsensortracking.SensorFixMapperFactory;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackingDataLoader;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;

/**
 * This class manages the lifecycle of the {@link RaceLogFixTracker} by listening to
 * {@link RaceLogDenoteForTrackingEvent}, race log attached and stop tracking race changes.
 * 
 * Once the race is stopped, it notifies its own {@link Owner} so that all reference to this instance can be cleanly
 * removed to prevent memory leaks.
 */
public class RaceLogFixTrackerManager implements TrackingDataLoader {
    private static final Logger logger = Logger.getLogger(RaceLogFixTrackerManager.class.getName());

    private final DynamicTrackedRace trackedRace;

    private final SensorFixStore sensorFixStore;

    private final SensorFixMapperFactory sensorFixMapperFactory;

    private final Owner owner;
    
    private RaceLogFixTracker tracker;

    private final RaceChangeListener raceChangeListener = new AbstractRaceChangeListener() {
        public void raceLogAttached(RaceLog raceLog) {
            raceLog.addListener(raceLogEventVisitor);

            updateDenotionState();
        }
        
        public void raceLogDetached(RaceLog raceLog) {
            raceLog.removeListener(raceLogEventVisitor);
            
            updateDenotionState();
        }

        /**
         * Stops tracking the races.
         * 
         * @param preemptive
         *            if <code>false</code>, the tracker will continue to process data already received but will stop
         *            receiving new data. If <code>true</code>, the tracker will stop processing data immediately,
         *            ignoring (dropping) all data already received but not yet processed.
         */
        public void stopTracking(boolean preemptive) {
            logger.fine("Got the signal to stop fix tracker for TrackedRace: " + trackedRace.getRaceIdentifier()
                    + "; preemptive: " + preemptive);
            stop(preemptive);
            owner.stopped(RaceLogFixTrackerManager.this);
        }
    };

    private final RaceLogEventVisitor raceLogEventVisitor = new BaseRaceLogEventVisitor() {
        @Override
        public void visit(RaceLogDenoteForTrackingEvent event) {
            updateDenotionState();
        }

        @Override
        public void visit(RaceLogRevokeEvent event) {
            updateDenotionState();
        }
    };

    /**
     * A callback that is used to inform when the {@link RaceLogFixTrackerManager} is stopped. This can be used to
     * cleanup any reference that holds an instance of the {@link RaceLogFixTrackerManager}.
     */
    public interface Owner {
        void stopped(RaceLogFixTrackerManager tracker);
    }

    public RaceLogFixTrackerManager(DynamicTrackedRace trackedRace, SensorFixStore sensorFixStore,
            SensorFixMapperFactory sensorFixMapperFactory) {
        this(trackedRace, sensorFixStore, sensorFixMapperFactory, (tracker) -> {});
    }
    
    public RaceLogFixTrackerManager(DynamicTrackedRace trackedRace, SensorFixStore sensorFixStore,
            SensorFixMapperFactory sensorFixMapperFactory, Owner owner) {
        this.trackedRace = trackedRace;
        this.sensorFixStore = sensorFixStore;
        this.sensorFixMapperFactory = sensorFixMapperFactory;
        this.owner = owner;

        trackedRace.addListener(raceChangeListener);
        
        for (RaceLog raceLog : trackedRace.getAttachedRaceLogs()) {
            raceLog.addListener(raceLogEventVisitor);
        }
        
        updateDenotionState();
    }

    private synchronized void updateDenotionState() {
        if (getForTracking()) {
            startTracker();
        } else {
            stopTracker(false);
        }
    }
    
    public void stop(boolean preemptive) {
        stopTracker(preemptive);
        trackedRace.removeListener(raceChangeListener);
        for (RaceLog raceLog : trackedRace.getAttachedRaceLogs()) {
            raceLog.removeListener(raceLogEventVisitor);
        }
    }
    
    private synchronized void startTracker() {
        if (tracker == null) {
            logger.fine("Starting fix tracker for TrackedRace: " + trackedRace.getRaceIdentifier());
            tracker = new RaceLogFixTracker(trackedRace, sensorFixStore, sensorFixMapperFactory);
        }
    }

    private synchronized void stopTracker(boolean preemptive) {
        if (tracker != null) {
            logger.fine("Stopping fix tracker for TrackedRace: " + trackedRace.getRaceIdentifier());
            tracker.stop(preemptive);
            tracker = null;
        }
    }

    boolean getForTracking() {
        for (RaceLog raceLog : trackedRace.getAttachedRaceLogs()) {
            RaceLogTrackingState raceLogTrackingState = new RaceLogTrackingStateAnalyzer(raceLog).analyze();
            if (raceLogTrackingState.isForTracking()) {
                return true;
            }
        }
        return false;
    }
}
