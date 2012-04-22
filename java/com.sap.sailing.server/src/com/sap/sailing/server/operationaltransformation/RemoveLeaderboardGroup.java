package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class RemoveLeaderboardGroup extends AbstractLeaderboardGroupOperation {
    private static final long serialVersionUID = 3177217793859319236L;

    public RemoveLeaderboardGroup(String leaderboardGroupName) {
        super(leaderboardGroupName);
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

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.removeLeaderboardGroup(getLeaderboardGroupName());
        return toState;
    }

}
