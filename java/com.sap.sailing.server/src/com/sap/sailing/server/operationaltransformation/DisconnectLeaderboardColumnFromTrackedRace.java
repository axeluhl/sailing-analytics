package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class DisconnectLeaderboardColumnFromTrackedRace extends AbstractLeaderboardColumnOperation<Void> {

    private static final long serialVersionUID = -5822713961135743309L;
    private final String fleetName;

    public DisconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String columnName, String fleetName) {
        super(leaderboardName, columnName);
        this.fleetName = fleetName;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        Leaderboard leaderboard = toState.getLeaderboardByName(getLeaderboardName());
        if (leaderboard != null) {
            RaceColumn raceColumn = leaderboard.getRaceColumnByName(getColumnName());
            if (raceColumn != null) {
                raceColumn.removeRaceIdentifier(raceColumn.getFleetByName(fleetName));
                updateDB(toState, leaderboard, raceColumn);
            } else {
                throw new IllegalArgumentException("Didn't find race "+getColumnName()+" in leaderboard "+getLeaderboardName());
            }
        } else {
            throw new IllegalArgumentException("Didn't find leaderboard "+getLeaderboardName());
        }
        return null;
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

}
