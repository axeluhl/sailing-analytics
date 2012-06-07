package com.sap.sailing.server.operationaltransformation;

import java.util.logging.Logger;

import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class CreateFlexibleLeaderboard extends AbstractLeaderboardOperation<FlexibleLeaderboard> {
    private static final Logger logger = Logger.getLogger(CreateFlexibleLeaderboard.class.getName());
    private static final long serialVersionUID = 891352705068098580L;
    private final int[] discardThresholds;

    public CreateFlexibleLeaderboard(String leaderboardName, int[] discardThresholds) {
        super(leaderboardName);
        this.discardThresholds = discardThresholds;
    }

    @Override
    public FlexibleLeaderboard internalApplyTo(RacingEventService toState) {
        FlexibleLeaderboard result = null;
        if (toState.getLeaderboardByName(getLeaderboardName()) == null) {
            result = toState.addFlexibleLeaderboard(getLeaderboardName(), discardThresholds);
        } else {
            logger.warning("Cannot replicate creation of flexible leaderboard "+getLeaderboardName()+" because it already exists in the replica");
        }
        return result;
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
