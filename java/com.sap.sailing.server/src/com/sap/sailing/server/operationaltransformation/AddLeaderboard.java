package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class AddLeaderboard extends AbstractLeaderboardOperation {
    private static final long serialVersionUID = 891352705068098580L;
    private final int[] discardThresholds;

    public AddLeaderboard(String leaderboardName, int[] discardThresholds) {
        super(leaderboardName);
        this.discardThresholds = discardThresholds;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.addLeaderboard(getLeaderboardName(), discardThresholds);
        return toState;
    }

    @Override
    public RacingEventServiceOperation transformClientOp(RacingEventServiceOperation serverOp) {
        return serverOp.transformAddLeaderboardClientOp(this);
    }

    @Override
    public RacingEventServiceOperation transformServerOp(RacingEventServiceOperation clientOp) {
        return clientOp.transformAddLeaderboardServerOp(this);
    }

}
