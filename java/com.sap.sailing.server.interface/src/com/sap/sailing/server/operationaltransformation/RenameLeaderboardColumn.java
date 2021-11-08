package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class RenameLeaderboardColumn extends AbstractLeaderboardColumnOperation<Void> {
    private static final long serialVersionUID = -1238503068559477351L;
    private final String newColumnName;
    
    public RenameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName) {
        super(leaderboardName, oldColumnName);
        this.newColumnName = newColumnName;
    }


    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.renameLeaderboardColumn(getLeaderboardName(), getColumnName(), newColumnName);
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return serverOp.transformRenameLeaderboardColumnClientOp(this);
    }


    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return clientOp.transformRenameLeaderboardColumnServerOp(this);
    }

}
