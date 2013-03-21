package com.sap.sailing.server.operationaltransformation;

import java.util.List;

import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateLeaderboardGroup extends AbstractLeaderboardGroupOperation<Void> {
    private static final long serialVersionUID = -1822477339916802467L;
    private final String newName;
    private final String newDescription;
    private final List<String> leaderboardNames;
    private final int[] overallLeaderboardDiscardThresholds;
    private final ScoringSchemeType overallLeaderboardScoringSchemeType;

    public UpdateLeaderboardGroup(String leaderboardGroupName, String newName, String newDescription,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType) {
        super(leaderboardGroupName);
        this.newName = newName;
        this.newDescription = newDescription;
        this.leaderboardNames = leaderboardNames;
        this.overallLeaderboardDiscardThresholds = overallLeaderboardDiscardThresholds;
        this.overallLeaderboardScoringSchemeType = overallLeaderboardScoringSchemeType;
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
        toState.updateLeaderboardGroup(getLeaderboardGroupName(), newName, newDescription, leaderboardNames,
                overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType);
        return null;
    }

}
