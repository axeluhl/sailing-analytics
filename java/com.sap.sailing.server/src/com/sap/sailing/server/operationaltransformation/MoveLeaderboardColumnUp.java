package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class MoveLeaderboardColumnUp extends AbstractLeaderboardColumnOperation {
    
    public MoveLeaderboardColumnUp(String leaderboardName, String columnName) {
        super(leaderboardName, columnName);
    }


    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.moveLeaderboardColumnUp(getLeaderboardName(), getColumnName());
        return toState;
    }


    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        // TODO Auto-generated method stub
        return null;
    }

}
