package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class RenameLeaderboardColumn extends AbstractLeaderboardColumnOperation {
    private final String newColumnName;
    
    public RenameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName) {
        super(leaderboardName, oldColumnName);
        this.newColumnName = newColumnName;
    }


    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.renameLeaderboardColumn(getLeaderboardName(), getColumnName(), newColumnName);
        return toState;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        return serverOp.transformClientRenameLeaderboardColumnOp(this);
    }


    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        return clientOp.transformServerRenameLeaderboardColumnOp(this);
    }

}
