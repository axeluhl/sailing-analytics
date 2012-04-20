package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventService;

public class CreateLeaderboard extends AbstractLeaderboardOperation<Leaderboard> {
    private static final long serialVersionUID = 891352705068098580L;
    private final int[] discardThresholds;

    public CreateLeaderboard(String leaderboardName, int[] discardThresholds) {
        super(leaderboardName);
        this.discardThresholds = discardThresholds;
    }

    @Override
    public Leaderboard internalApplyTo(RacingEventService toState) {
        return toState.addLeaderboard(getLeaderboardName(), discardThresholds);
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return serverOp.transformAddLeaderboardClientOp(this);
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return clientOp.transformAddLeaderboardServerOp(this);
    }

}
