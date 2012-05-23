package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class ConnectTrackedRaceToLeaderboardColumn extends AbstractLeaderboardColumnOperation<Boolean> {
    private static final long serialVersionUID = -1336511401516212508L;
    private final RaceIdentifier raceToConnect;
    private final String fleetName;
    
    public ConnectTrackedRaceToLeaderboardColumn(String leaderboardName, String columnName, String fleetName, RaceIdentifier raceToConnect) {
        super(leaderboardName, columnName);
        this.raceToConnect = raceToConnect;
        this.fleetName = fleetName;
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
            RaceColumn raceColumn = leaderboard.getRaceColumnByName(getColumnName());
            if (raceColumn != null) {
                TrackedRace trackedRace = toState.getExistingTrackedRace(raceToConnect);
                if (trackedRace != null) {
                    raceColumn.setTrackedRace(raceColumn.getFleetByName(fleetName), trackedRace);
                } else {
                    raceColumn.setRaceIdentifier(raceColumn.getFleetByName(fleetName), raceToConnect);
                }
            }
            success = true;
            toState.updateStoredLeaderboard(leaderboard);
        }
        return success;
    }

}
