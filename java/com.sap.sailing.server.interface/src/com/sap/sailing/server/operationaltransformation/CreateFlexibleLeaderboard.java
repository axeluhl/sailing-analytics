package com.sap.sailing.server.operationaltransformation;

import java.io.Serializable;
import java.util.logging.Logger;

import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class CreateFlexibleLeaderboard extends AbstractLeaderboardOperation<FlexibleLeaderboard> {
    private static final Logger logger = Logger.getLogger(CreateFlexibleLeaderboard.class.getName());
    private static final long serialVersionUID = 891352705068098580L;
    private final int[] discardThresholds;
    private final ScoringScheme scoringScheme;
    private final String leaderboardDisplayName;
    private final Serializable courseAreaId;

    public CreateFlexibleLeaderboard(String leaderboardName, String leaderboardDisplayName, int[] discardThresholds, ScoringScheme scoringScheme, Serializable courseAreaId) {
        super(leaderboardName);
        this.leaderboardDisplayName = leaderboardDisplayName;
        this.discardThresholds = discardThresholds;
        this.scoringScheme = scoringScheme;
        this.courseAreaId = courseAreaId;
    }

    @Override
    public FlexibleLeaderboard internalApplyTo(RacingEventService toState) {
        FlexibleLeaderboard result = null;
        if (toState.getLeaderboardByName(getLeaderboardName()) == null) {
            result = toState.addFlexibleLeaderboard(getLeaderboardName(), leaderboardDisplayName, discardThresholds, scoringScheme, courseAreaId);
        } else {
            logger.warning("Cannot replicate creation of flexible leaderboard "+getLeaderboardName()+" because it already exists in the replica");
        }
        return result;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        return serverOp.transformAddFlexibleLeaderboardClientOp(this);
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        return clientOp.transformAddFlexibleLeaderboardServerOp(this);
    }
}
