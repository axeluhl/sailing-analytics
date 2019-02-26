package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class RenameLeaderboard extends AbstractLeaderboardOperation<Void> {
    private static final long serialVersionUID = 8874599127010526971L;

    private final String newLeaderboardName;
    
    public RenameLeaderboard(String leaderboardName, String newLeaderboardName) {
        super(leaderboardName);
        this.newLeaderboardName = newLeaderboardName;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        if (!getLeaderboardName().equals(newLeaderboardName)) {
            toState.renameLeaderboard(getLeaderboardName(), newLeaderboardName);
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
