package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class MoveLeaderboardColumnDown implements RacingEventServiceOperation {
    private final String leaderboardName;
    private final String columnName;
    
    public MoveLeaderboardColumnDown(String leaderboardName, String columnName) {
        super();
        this.leaderboardName = leaderboardName;
        this.columnName = columnName;
    }


    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.moveLeaderboardColumnDown(leaderboardName, columnName);
        return toState;
    }


    @Override
    public RacingEventServiceOperation transformFor(RacingEventServiceOperation peerOp) {
        // TODO Auto-generated method stub
        return null;
    }
}
