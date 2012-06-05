package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class CreateFlexibleLeaderboard extends AbstractLeaderboardOperation<FlexibleLeaderboard> {
    private static final long serialVersionUID = 891352705068098580L;
    private final int[] discardThresholds;

    public CreateFlexibleLeaderboard(String leaderboardName, int[] discardThresholds) {
        super(leaderboardName);
        this.discardThresholds = discardThresholds;
    }

    @Override
    public FlexibleLeaderboard internalApplyTo(RacingEventService toState) {
        return toState.addFlexibleLeaderboard(getLeaderboardName(), discardThresholds);
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
