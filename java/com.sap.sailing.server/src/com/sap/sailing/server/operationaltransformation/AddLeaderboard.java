package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.server.RacingEventService;

public class AddLeaderboard extends AbstractRacingEventServiceOperation {
    private final String leaderboardName;
    private final int[] discardThresholds;

    public AddLeaderboard(String leaderboardName, int[] discardThresholds) {
        super();
        this.leaderboardName = leaderboardName;
        this.discardThresholds = discardThresholds;
    }

    @Override
    public RacingEventService applyTo(RacingEventService toState) {
        toState.addLeaderboard(leaderboardName, discardThresholds);
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
