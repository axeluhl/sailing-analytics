package com.sap.sailing.server.operationaltransformation;

import java.util.List;

import com.sap.sailing.server.RacingEventService;

public class UpdateLeaderboardGroup extends AbstractLeaderboardGroupOperation<Void> {
    private static final long serialVersionUID = -1822477339916802467L;
    private final String newName;
    private final String newDescription;
    private final List<String> leaderboardNames;

    public UpdateLeaderboardGroup(String leaderboardGroupName, String newName, String newDescription,
            List<String> leaderboardNames) {
        super(leaderboardGroupName);
        this.newName = newName;
        this.newDescription = newDescription;
        this.leaderboardNames = leaderboardNames;
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

    @Override
    public Void internalApplyTo(RacingEventService toState) {
        toState.updateLeaderboardGroup(getLeaderboardGroupName(), newName, newDescription, leaderboardNames);
        return null;
    }

}
