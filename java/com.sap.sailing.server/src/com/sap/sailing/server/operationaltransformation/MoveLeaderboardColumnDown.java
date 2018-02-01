package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class MoveLeaderboardColumnDown extends AbstractLeaderboardColumnOperation<Void> {
    
    private static final long serialVersionUID = -1041804872669106579L;

    public MoveLeaderboardColumnDown(String leaderboardName, String columnName) {
        super(leaderboardName, columnName);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.moveLeaderboardColumnDown(getLeaderboardName(), getColumnName());
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return serverOp.transformMoveLeaderboardColumnDownClientOp(this);
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return clientOp.transformMoveLeaderboardColumnDownServerOp(this);
    }

}
