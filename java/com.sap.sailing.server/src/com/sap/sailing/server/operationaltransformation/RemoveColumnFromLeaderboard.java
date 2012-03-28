package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.operationaltransformation.Operation;
import com.sap.sailing.server.RacingEventService;

public class RemoveColumnFromLeaderboard implements Operation<RacingEventService> {
    private final String columnName;
    private final String leaderboardName;
    
    
    public RemoveColumnFromLeaderboard(String columnName, String leaderboardName) {
        super();
        this.columnName = columnName;
        this.leaderboardName = leaderboardName;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.removeLeaderboardColumn(leaderboardName, columnName);
        return toState;
    }
}
