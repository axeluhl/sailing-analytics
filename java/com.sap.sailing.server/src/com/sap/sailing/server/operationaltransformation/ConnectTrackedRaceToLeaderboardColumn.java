package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class ConnectTrackedRaceToLeaderboardColumn extends AbstractLeaderboardColumnOperation<Boolean> {
    private static final long serialVersionUID = -1336511401516212508L;
    private final RaceIdentifier raceToConnect;
    
    public ConnectTrackedRaceToLeaderboardColumn(String leaderboardName, String columnName, RaceIdentifier raceToConnect) {
        super(leaderboardName, columnName);
        this.raceToConnect = raceToConnect;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean internalApplyTo(RacingEventService toState) {
        boolean success = false;
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        if (leaderboard != null) {
            RaceInLeaderboard raceColumn = leaderboard.getRaceColumnByName(getColumnName());
            if (raceColumn != null) {
                TrackedRace trackedRace = toState.getExistingTrackedRace(raceToConnect);
                if (trackedRace != null) {
                    raceColumn.setTrackedRace(trackedRace);
                }
            }
            raceColumn.setRaceIdentifier(raceToConnect);
            success = true;
            toState.updateStoredLeaderboard(leaderboard);
        }
        return success;
    }

}
