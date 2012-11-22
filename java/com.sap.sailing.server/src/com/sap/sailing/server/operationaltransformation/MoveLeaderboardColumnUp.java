package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class MoveLeaderboardColumnUp extends AbstractLeaderboardColumnOperation<Void> {
    
    private static final long serialVersionUID = 8444485841005051004L;

    public MoveLeaderboardColumnUp(String leaderboardName, String columnName) {
        super(leaderboardName, columnName);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.moveLeaderboardColumnUp(getLeaderboardName(), getColumnName());
        return null;
    }


    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return serverOp.transformMoveLeaderboardColumnUpClientOp(this);
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return clientOp.transformMoveLeaderboardColumnUpServerOp(this);
    }

}
