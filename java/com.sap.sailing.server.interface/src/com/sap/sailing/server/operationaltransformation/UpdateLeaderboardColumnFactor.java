package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class UpdateLeaderboardColumnFactor extends AbstractLeaderboardColumnOperation<Void> {
    private static final long serialVersionUID = -1238503068559477351L;
    private final Double newFactor;
    
    public UpdateLeaderboardColumnFactor(String leaderboardName, String columnName, Double newFactor) {
        super(leaderboardName, columnName);
        this.newFactor = newFactor;
    }


    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.updateLeaderboardColumnFactor(getLeaderboardName(), getColumnName(), newFactor);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO
        return null;
    }


    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO
        return null;
    }

}
