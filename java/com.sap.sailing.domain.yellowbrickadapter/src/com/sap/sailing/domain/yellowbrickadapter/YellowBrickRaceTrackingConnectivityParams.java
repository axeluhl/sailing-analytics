package com.sap.sailing.domain.yellowbrickadapter;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupResolver;
import com.sap.sailing.domain.racelog.RaceLogAndTrackedRaceResolver;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingHandler;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.impl.AbstractRaceTrackingConnectivityParameters;
import com.sap.sailing.domain.yellowbrickadapter.impl.YellowBrickRaceTrackerImpl;

public class YellowBrickRaceTrackingConnectivityParams extends AbstractRaceTrackingConnectivityParameters {
    private static final long serialVersionUID = -81948107186932864L;
    
    private final String raceUrl;
    private final String username;
    private final String password;

    public YellowBrickRaceTrackingConnectivityParams(String raceUrl, String username, String password,
            boolean trackWind, boolean correctWindDirectionByMagneticDeclination) {
        super(trackWind, correctWindDirectionByMagneticDeclination);
        this.raceUrl = raceUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            RaceLogAndTrackedRaceResolver raceLogResolver, LeaderboardGroupResolver leaderboardGroupResolver,
            long timeoutInMilliseconds, RaceTrackingHandler raceTrackingHandler) throws Exception {
        return new YellowBrickRaceTrackerImpl(this);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry,
            WindStore windStore, RaceLogAndTrackedRaceResolver raceLogResolver,
            LeaderboardGroupResolver leaderboardGroupResolver, long timeoutInMilliseconds,
            RaceTrackingHandler raceTrackingHandler) throws Exception {
        // TODO Implement YellowBrickRaceTrackingConnectivityParams.createRaceTracker(...)
        return null;
    }

    @Override
    public Object getTrackerID() {
        // TODO Implement YellowBrickRaceTrackingConnectivityParams.getTrackerID(...)
        return null;
    }

    @Override
    public long getDelayToLiveInMillis() {
        // TODO Implement YellowBrickRaceTrackingConnectivityParams.getDelayToLiveInMillis(...)
        return 0;
    }

    @Override
    public String getTypeIdentifier() {
        // TODO Implement YellowBrickRaceTrackingConnectivityParams.getTypeIdentifier(...)
        return null;
    }

}
