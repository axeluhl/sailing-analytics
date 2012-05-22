package com.sap.sailing.domain.swisstimingadapter.impl;

import com.sap.sailing.domain.base.Regatta;
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
    private final WindStore windStore;
    private final RaceSpecificMessageLoader messageLoader; 
    
    public SwissTimingTrackingConnectivityParameters(String hostname, int port, String raceID, boolean canSendRequests,
            SwissTimingFactory swissTimingFactory, DomainFactory domainFactory, WindStore windStore,
            RaceSpecificMessageLoader messageLoader) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.raceID = raceID;
        this.canSendRequests = canSendRequests;
        this.swissTimingFactory = swissTimingFactory;
        this.domainFactory = domainFactory;
        this.windStore = windStore;
        this.messageLoader = messageLoader;
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry) throws Exception {
        return swissTimingFactory.createRaceTracker(raceID, hostname, port, canSendRequests, windStore, messageLoader,
                domainFactory, trackedRegattaRegistry);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry)
            throws Exception {
        return swissTimingFactory.createRaceTracker(regatta, raceID, hostname, port, canSendRequests, windStore, messageLoader,
                domainFactory, trackedRegattaRegistry);
    }

    @Override
    public Object getTrackerID() {
        return SwissTimingRaceTrackerImpl.createID(raceID, hostname, port);
    }

}
