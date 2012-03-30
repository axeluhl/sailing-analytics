package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class MoveLeaderboardColumnDown extends AbstractLeaderboardColumnOperation {
    
    public MoveLeaderboardColumnDown(String leaderboardName, String columnName) {
        super(leaderboardName, columnName);
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.moveLeaderboardColumnDown(getLeaderboardName(), getColumnName());
        return toState;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        return serverOp.transformMoveLeaderboardColumnDownClientOp(this);
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        return clientOp.transformMoveLeaderboardColumnDownServerOp(this);
    }

}
