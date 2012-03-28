package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.operationaltransformation.Operation;
import com.sap.sailing.server.RacingEventService;

public class RenameLeaderboardColumn implements Operation<RacingEventService> {
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
}
