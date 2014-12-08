package com.sap.sailing.domain.racelogtracking.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceInformationFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.racelog.tracking.RaceNotCreatedException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.server.RacingEventService;

public class RaceLogConnectivityParams implements RaceTrackingConnectivityParameters {
    private final RacingEventService service;
    private final RaceColumn raceColumn;
    private final Fleet fleet;
    private final Leaderboard leaderboard;
    private final long delayToLiveInMillis;
    private final Regatta regatta;
    private final DomainFactory domainFactory;

    public RaceLogConnectivityParams(RacingEventService service, Regatta regatta, RaceColumn raceColumn, Fleet fleet,
            Leaderboard leaderboard, long delayToLiveInMillis, DomainFactory domainFactory) throws RaceNotCreatedException {
        this.service = service;
        this.regatta = regatta;
        this.raceColumn = raceColumn;
        this.fleet = fleet;
        this.leaderboard = leaderboard;
        this.delayToLiveInMillis = delayToLiveInMillis;
        this.domainFactory = domainFactory;

        if (!new RaceLogTrackingStateAnalyzer(getRaceLog()).analyze().isForTracking()) {
            throw new RaceNotCreatedException(String.format("Racelog (%s) is not denoted for tracking", getRaceLog()));
        }
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            GPSFixStore gpsFixStore) {
        return createRaceTracker(regatta, trackedRegattaRegistry, windStore, gpsFixStore);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry,
            WindStore windStore, GPSFixStore gpsFixStore) {
        if (regatta == null) {
            BoatClass boatClass = new RaceInformationFinder(getRaceLog()).analyze().getBoatClass();
            regatta = service.getOrCreateDefaultRegatta(
                    RegattaImpl.getDefaultName("RaceLog-tracking default Regatta", boatClass.getName()),
                    boatClass.getName(), UUID.randomUUID());
        }
        if (regatta == null) {
            throw new RaceNotCreatedException("No regatta for race-log tracked race");
        }
        DynamicTrackedRegatta trackedRegatta = trackedRegattaRegistry.getOrCreateTrackedRegatta(regatta);
        return new RaceLogRaceTracker(trackedRegatta, this, windStore, gpsFixStore);
    }

    @Override
    public Object getTrackerID() {
        return getRaceLog().getId();
    }

    @Override
    public long getDelayToLiveInMillis() {
        return delayToLiveInMillis;
    }

    public RaceLog getRaceLog() {
        return raceColumn.getRaceLog(fleet);
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

    public RacingEventService getService() {
        return service;
    }
    
    public DomainFactory getDomainFactory() {
        return domainFactory;
    }
    
    public List<AbstractLog<?, ?>> getLogHierarchy() {
        List<AbstractLog<?, ?>> result = new ArrayList<>();
        result.add(getRaceLog());
        if (leaderboard instanceof IsRegattaLike) {
            result.add(((IsRegattaLike) leaderboard).getRegattaLog());
        }
        return result;
    }
}
