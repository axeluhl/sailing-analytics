package com.sap.sailing.domain.racelogtracking.impl.logtracker;

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

public class RaceLogSensorFixTrackerLifecycle implements TrackingDataLoader {
    private static final Logger logger = Logger.getLogger(RaceLogSensorFixTrackerLifecycle.class.getName());

    private final DynamicTrackedRace trackedRace;

    private final SensorFixStore sensorFixStore;

    private final SensorFixMapperFactory sensorFixMapperFactory;

    private final Owner owner;
    
    private RaceLogSensorFixTracker tracker;

    private final RaceChangeListener raceChangeListener = new AbstractRaceChangeListener() {
        public void raceLogAttached(RaceLog raceLog) {
            raceLog.addListener(raceLogEventVisitor);
        }

        public void stopTracking(boolean preemptive) {
            stop();
            owner.stopped(RaceLogSensorFixTrackerLifecycle.this);
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

    public interface Owner {
        void stopped(RaceLogSensorFixTrackerLifecycle tracker);
    }

    public RaceLogSensorFixTrackerLifecycle(DynamicTrackedRace trackedRace, SensorFixStore sensorFixStore,
            SensorFixMapperFactory sensorFixMapperFactory, Owner owner) {
        this.trackedRace = trackedRace;
        this.sensorFixStore = sensorFixStore;
        this.sensorFixMapperFactory = sensorFixMapperFactory;
        this.owner = owner;

        trackedRace.addListener(raceChangeListener);
        
        for (RaceLog raceLog : trackedRace.getAttachedRaceLogs()) {
            raceLog.addListener(raceLogEventVisitor);
        }
    }

    private synchronized void updateDenotionState() {
        boolean forTracking = getForTracking();
        if (tracker == null && forTracking) {
            tracker = new RaceLogSensorFixTracker(trackedRace, sensorFixStore, sensorFixMapperFactory);
        }
        if (tracker != null && !forTracking) {
            stopTracker();
        }

    }
    
    public void stop() {
        stopTracker();
        for (RaceLog raceLog : trackedRace.getAttachedRaceLogs()) {
            raceLog.removeListener(raceLogEventVisitor);
        }
    }

    private void stopTracker() {
        tracker.stop();
        tracker = null;
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
