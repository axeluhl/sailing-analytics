package com.sap.sailing.domain.swisstimingadapter.impl;


import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingConnectivityParameters;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;

public class SwissTimingTrackingConnectivityParameters implements RaceTrackingConnectivityParameters {
    public static final String TYPE = "SWISS_TIMING";
    
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
    private final boolean useInternalMarkPassingAlgorithm;
    
    public SwissTimingTrackingConnectivityParameters(String hostname, int port, String raceID, String raceName,
            String raceDescription, BoatClass boatClass, StartList startList, long delayToLiveInMillis,
            SwissTimingFactory swissTimingFactory, DomainFactory domainFactory, RaceLogStore raceLogStore,
            RegattaLogStore regattaLogStore, boolean useInternalMarkPassingAlgorithm) {
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
        this.useInternalMarkPassingAlgorithm = useInternalMarkPassingAlgorithm;
    }
    
    @Override
    public String getTypeIdentifier() {
        return TYPE;
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            RaceLogResolver raceLogResolver) throws Exception {
        return swissTimingFactory.createRaceTracker(raceID, raceName, raceDescription, boatClass, hostname, port,
                startList, delayToLiveInMillis, raceLogStore, regattaLogStore, windStore,
                useInternalMarkPassingAlgorithm,
                domainFactory, trackedRegattaRegistry, raceLogResolver);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry,
            WindStore windStore, RaceLogResolver raceLogResolver) throws Exception {
        return swissTimingFactory.createRaceTracker(regatta, raceID, raceName, raceDescription, boatClass, hostname,
                port, startList, delayToLiveInMillis, windStore, useInternalMarkPassingAlgorithm,
                domainFactory, trackedRegattaRegistry, raceLogResolver, raceLogStore, regattaLogStore);
    }

    @Override
    public Object getTrackerID() {
        return SwissTimingRaceTrackerImpl.createID(raceID, hostname, port);
    }

    @Override
    public long getDelayToLiveInMillis() {
        return delayToLiveInMillis;
    }

    String getHostname() {
        return hostname;
    }

    int getPort() {
        return port;
    }

    String getRaceID() {
        return raceID;
    }

    String getRaceName() {
        return raceName;
    }

    String getRaceDescription() {
        return raceDescription;
    }

    BoatClass getBoatClass() {
        return boatClass;
    }

    SwissTimingFactory getSwissTimingFactory() {
        return swissTimingFactory;
    }

    DomainFactory getDomainFactory() {
        return domainFactory;
    }

    RaceLogStore getRaceLogStore() {
        return raceLogStore;
    }

    RegattaLogStore getRegattaLogStore() {
        return regattaLogStore;
    }

    StartList getStartList() {
        return startList;
    }

    boolean isUseInternalMarkPassingAlgorithm() {
        return useInternalMarkPassingAlgorithm;
    }
}
