package com.sap.sailing.domain.swisstimingadapter.impl;


import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupResolver;
import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.regattalog.RegattaLogStore;
import com.sap.sailing.domain.swisstimingadapter.DomainFactory;
import com.sap.sailing.domain.swisstimingadapter.StartList;
import com.sap.sailing.domain.swisstimingadapter.SwissTimingFactory;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.impl.AbstractRaceTrackingConnectivityParameters;

public class SwissTimingTrackingConnectivityParameters extends AbstractRaceTrackingConnectivityParameters {
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
    private final String updateURL;
    private final String updateUsername;
    private final String updatePassword;
    
    public SwissTimingTrackingConnectivityParameters(String hostname, int port, String raceID, String raceName,
            String raceDescription, BoatClass boatClass, StartList startList, long delayToLiveInMillis,
            SwissTimingFactory swissTimingFactory, DomainFactory domainFactory, RaceLogStore raceLogStore,
            RegattaLogStore regattaLogStore, boolean useInternalMarkPassingAlgorithm, boolean trackWind,
            boolean correctWindDirectionByMagneticDeclination, String updateURL, String updateUsername, String updatePassword) {
        super(trackWind, correctWindDirectionByMagneticDeclination);
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
        this.updateURL = updateURL;
        this.updateUsername = updateUsername;
        this.updatePassword = updatePassword;
    }
    
    @Override
    public String getTypeIdentifier() {
        return TYPE;
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            RaceLogResolver raceLogResolver, LeaderboardGroupResolver leaderboardGroupResolver, long timeoutInMilliseconds) throws Exception {
        return swissTimingFactory.createRaceTracker(raceLogStore, regattaLogStore, windStore, domainFactory, trackedRegattaRegistry, raceLogResolver,
                this);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry,
            WindStore windStore, RaceLogResolver raceLogResolver, LeaderboardGroupResolver leaderboardGroupResolver, long timeoutInMilliseconds) throws Exception {
        return swissTimingFactory.createRaceTracker(regatta, windStore, domainFactory, trackedRegattaRegistry, raceLogResolver, raceLogStore,
                regattaLogStore, this);
    }

    @Override
    public Object getTrackerID() {
        return SwissTimingRaceTrackerImpl.createID(raceID, hostname, port);
    }

    @Override
    public long getDelayToLiveInMillis() {
        return delayToLiveInMillis;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getUpdateURL() {
        return updateURL;
    }

    public String getUpdateUsername() {
        return updateUsername;
    }

    public String getUpdatePassword() {
        return updatePassword;
    }

    public String getRaceID() {
        return raceID;
    }

    public String getRaceName() {
        return raceName;
    }

    public String getRaceDescription() {
        return raceDescription;
    }

    public BoatClass getBoatClass() {
        return boatClass;
    }

    public StartList getStartList() {
        return startList;
    }

    public boolean isUseInternalMarkPassingAlgorithm() {
        return useInternalMarkPassingAlgorithm;
    }
}
