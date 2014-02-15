package com.sap.sailing.domain.racelog.tracking.impl;

import java.util.UUID;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;

public class RaceLogConnectivityParams implements RaceTrackingConnectivityParameters {
    private final WindStore windStore;
    private final RaceLog raceLog;
    private final RaceColumn raceColumn;
    private final Fleet fleet;
    private final Leaderboard leaderboard;
    private final UUID id;
    private final long delayToLiveInMillis;
    private final RegattaIdentifier regatta;

    public RaceLogConnectivityParams(RegattaIdentifier regatta, WindStore windStore, RaceLog raceLog,
    		RaceColumn raceColumn, Fleet fleet, Leaderboard leaderboard, long delayToLiveInMillis) {
    	this.regatta = regatta;
        this.windStore = windStore;
        this.raceLog = raceLog;
        this.raceColumn = raceColumn;
        this.fleet = fleet;
        this.leaderboard = leaderboard;
        this.delayToLiveInMillis = delayToLiveInMillis;
        this.id = UUID.randomUUID();
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore) throws Exception {
        return createRaceTracker(trackedRegattaRegistry.getTrackedRegatta(regatta), trackedRegattaRegistry, windStore);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore)
            throws Exception {
        return new RaceLogRaceTracker(this, regatta);
    }
    
    public void 

    @Override
    public Object getTrackerID() {
        return id;
    }

    @Override
    public long getDelayToLiveInMillis() {
        return delayToLiveInMillis;
    }

    public WindStore getWindStore() {
        return windStore;
    }

    public RaceLog getRaceLog() {
        return raceLog;
    }

    public RaceColumn getRaceColumn() {
        return raceColumn;
    }

    public Fleet getFleet() {
        return fleet;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
}
