package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RenameLeaderboardGroup extends AbstractLeaderboardGroupOperation<Void> {
    private static final long serialVersionUID = -5007784579411727148L;
    private final String newLeaderboardGroupName;

    public RenameLeaderboardGroup(String leaderboardGroupName, String newLeaderboardGroupName) {
        super(leaderboardGroupName);
        this.newLeaderboardGroupName = newLeaderboardGroupName;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        // TODO see bug 729: try to move addLeaderboardGroup implementation here and synthesize and apply this operation there
        if (!getLeaderboardGroupName().equals(newLeaderboardGroupName)) {
            toState.renameLeaderboardGroup(getLeaderboardGroupName(), newLeaderboardGroupName);
        }
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
