package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class AddColumnToLeaderboard implements RacingEventServiceOperation {
    private final String columnName;
    private final String leaderboardName;
    private final boolean medalRace;
    
    
    public AddColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace) {
        super();
        this.columnName = columnName;
        this.leaderboardName = leaderboardName;
        this.medalRace = medalRace;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.addColumnToLeaderboard(columnName, leaderboardName, medalRace);
        return toState;
    }

    @Override
    public RacingEventServiceOperation transformFor(RacingEventServiceOperation peerOp) {
        // TODO Auto-generated method stub
        return null;
    }
}
