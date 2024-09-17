package com.sap.sailing.domain.queclinkadapter.tracker;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupResolver;
import com.sap.sailing.domain.markpassinghash.MarkPassingRaceFingerprintRegistry;
import com.sap.sailing.domain.racelog.RaceLogAndTrackedRaceResolver;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.RaceTrackingHandler;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.impl.AbstractRaceTrackingConnectivityParameters;

public class QueclinkConnectivityParameters extends AbstractRaceTrackingConnectivityParameters {
    private static final long serialVersionUID = -3538618122514935837L;

    public QueclinkConnectivityParameters(boolean trackWind, boolean correctWindDirectionByMagneticDeclination) {
        super(trackWind, correctWindDirectionByMagneticDeclination);
        // TODO Auto-generated constructor stub
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            RaceLogAndTrackedRaceResolver raceLogResolver, LeaderboardGroupResolver leaderboardGroupResolver,
            long timeoutInMilliseconds, RaceTrackingHandler raceTrackingHandler,
            MarkPassingRaceFingerprintRegistry markPassingRaceFingerprintRegistry) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry,
            WindStore windStore, RaceLogAndTrackedRaceResolver raceLogResolver,
            LeaderboardGroupResolver leaderboardGroupResolver, long timeoutInMilliseconds,
            RaceTrackingHandler raceTrackingHandler,
            MarkPassingRaceFingerprintRegistry markPassingRaceFingerprintRegistry) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getTrackerID() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getDelayToLiveInMillis() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getTypeIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

}
