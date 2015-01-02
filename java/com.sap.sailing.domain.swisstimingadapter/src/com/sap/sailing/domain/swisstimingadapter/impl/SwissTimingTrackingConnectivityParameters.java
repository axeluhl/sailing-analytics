package com.sap.sailing.domain.swisstimingadapter.impl;


import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;

public class SwissTimingTrackingConnectivityParameters implements RaceTrackingConnectivityParameters {
    private final String hostname;
    private final int port;
    private final String raceID;
    private final String raceName;
    private final String raceDescription;
    private final BoatClass boatClass;
    private final SwissTimingFactory swissTimingFactory;
    private final DomainFactory domainFactory;
    private final RaceLogStore raceLogStore;
    private final RegattaLogStore regattaLogStore;
    private final long delayToLiveInMillis;
    private final StartList startList;
    
    public SwissTimingTrackingConnectivityParameters(String hostname, int port, String raceID, String raceName,
            String raceDescription, BoatClass boatClass, StartList startList, long delayToLiveInMillis,
            SwissTimingFactory swissTimingFactory, DomainFactory domainFactory, RaceLogStore raceLogStore, RegattaLogStore regattaLogStore) {
        super();
        this.hostname = hostname;
        this.port = port;
        this.raceID = raceID;
        this.raceName = raceName;
        this.raceDescription = raceDescription;
        this.boatClass = boatClass;
        this.startList = startList;
        this.delayToLiveInMillis = delayToLiveInMillis;
        this.swissTimingFactory = swissTimingFactory;
        this.domainFactory = domainFactory;
        this.raceLogStore = raceLogStore;
        this.regattaLogStore = regattaLogStore;
    }
    
    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            GPSFixStore gpsFixStore) throws Exception {
        return swissTimingFactory.createRaceTracker(raceID, raceName, raceDescription, boatClass, hostname, port,
                startList, delayToLiveInMillis, raceLogStore, regattaLogStore, windStore, gpsFixStore, domainFactory,
                trackedRegattaRegistry);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore, GPSFixStore gpsFixStore)
            throws Exception {
        return swissTimingFactory.createRaceTracker(regatta, raceID, raceName, raceDescription, boatClass, hostname, port, startList, delayToLiveInMillis, windStore, gpsFixStore, domainFactory,
                trackedRegattaRegistry);
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
