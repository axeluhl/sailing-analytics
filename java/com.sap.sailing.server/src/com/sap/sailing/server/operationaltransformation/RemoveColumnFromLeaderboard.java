package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class RemoveColumnFromLeaderboard extends AbstractLeaderboardColumnOperation {
    
    
    public RemoveColumnFromLeaderboard(String columnName, String leaderboardName) {
        super(leaderboardName, columnName);
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.removeLeaderboardColumn(getLeaderboardName(), getColumnName());
        return toState;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        return serverOp.transformRemoveColumnFromLeaderboardClientOp(this);
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        return clientOp.transformRemoveColumnFromLeaderboardServerOp(this);
    }

}
