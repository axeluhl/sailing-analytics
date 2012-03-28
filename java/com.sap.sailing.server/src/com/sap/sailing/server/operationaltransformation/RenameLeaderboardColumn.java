package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class RenameLeaderboardColumn implements RacingEventServiceOperation {
    private final String leaderboardName;
    private final String oldColumnName;
    private final String newColumnName;
    
    public RenameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName) {
        super();
        this.leaderboardName = leaderboardName;
        this.oldColumnName = oldColumnName;
        this.newColumnName = newColumnName;
    }


    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.renameLeaderboardColumn(leaderboardName, oldColumnName, newColumnName);
        return toState;
    }


    @Override
    public RacingEventServiceOperation transformFor(RacingEventServiceOperation peerOp) {
        // TODO Auto-generated method stub
        return null;
    }
}
