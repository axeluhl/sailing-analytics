package com.sap.sailing.domain.swisstimingadapter.impl;


import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.RaceSpecificMessageLoader;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;

public class SwissTimingTrackingConnectivityParameters implements RaceTrackingConnectivityParameters {
    private final String hostname;
    private final int port;
    private final String raceID;
    private final boolean canSendRequests;
    private final SwissTimingFactory swissTimingFactory;
    private final DomainFactory domainFactory;
    private final RaceLogStore raceLogStore;
    private final RaceSpecificMessageLoader messageLoader; 
    private final long delayToLiveInMillis;
    
    public SwissTimingTrackingConnectivityParameters(String hostname, int port, String raceID, boolean canSendRequests, long delayToLiveInMillis,
            SwissTimingFactory swissTimingFactory, DomainFactory domainFactory, RaceLogStore raceLogStore,
            RaceSpecificMessageLoader messageLoader) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.raceID = raceID;
        this.canSendRequests = canSendRequests;
        this.delayToLiveInMillis = delayToLiveInMillis;
        this.swissTimingFactory = swissTimingFactory;
        this.domainFactory = domainFactory;
        this.raceLogStore = raceLogStore;
        this.messageLoader = messageLoader;
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore) throws Exception {
        return swissTimingFactory.createRaceTracker(raceID, hostname, port, canSendRequests, delayToLiveInMillis, raceLogStore, windStore, messageLoader,
                domainFactory, trackedRegattaRegistry);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore)
            throws Exception {
        return swissTimingFactory.createRaceTracker(regatta, raceID, hostname, port, canSendRequests, delayToLiveInMillis, windStore, messageLoader,
                domainFactory, trackedRegattaRegistry);
    }

    @Override
    public Object getTrackerID() {
        return SwissTimingRaceTrackerImpl.createID(raceID, hostname, port);
    }

    @Override
    public long getDelayToLiveInMillis() {
        return delayToLiveInMillis;
    }
}
