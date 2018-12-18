package com.sap.sailing.server.operationaltransformation;

import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sailing.server.interfaces.RacingEventServiceOperation;

public class CreateLeaderboardGroup extends AbstractLeaderboardGroupOperation<LeaderboardGroup> {
    private static final long serialVersionUID = -5028997286564650805L;
    private final UUID id;
    private final String description;
    private final String displayName;
    private final boolean displayGroupsInReverseOrder;
    private final List<String> leaderboardNames;
    private final int[] overallLeaderboardDiscardThresholds;
    private final ScoringSchemeType overallLeaderboardScoringSchemeType;

    public CreateLeaderboardGroup(UUID id, String leaderboardGroupName, String description, String displayName,
            boolean displayGroupsInReverseOrder,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType) {
        super(leaderboardGroupName);
        this.id = id;
        this.description = description;
        this.displayName = displayName;
        this.displayGroupsInReverseOrder = displayGroupsInReverseOrder;
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
    public LeaderboardGroup internalApplyTo(RacingEventService toState) {
        // TODO see bug 729: try to move addLeaderboardGroup implementation here and synthesize and apply this operation there
        return toState.addLeaderboardGroup(id, getLeaderboardGroupName(), description, displayName,
                displayGroupsInReverseOrder, leaderboardNames, overallLeaderboardDiscardThresholds, overallLeaderboardScoringSchemeType);
    }

}
