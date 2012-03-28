package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.operationaltransformation.Operation;
import com.sap.sailing.server.RacingEventService;

public class MoveLeaderboardColumnUp implements Operation<RacingEventService> {
    private final String leaderboardName;
    private final String columnName;
    
    public MoveLeaderboardColumnUp(String leaderboardName, String columnName) {
        super();
        this.leaderboardName = leaderboardName;
        this.columnName = columnName;
    }


    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.moveLeaderboardColumnUp(leaderboardName, columnName);
        return toState;
    }
}
