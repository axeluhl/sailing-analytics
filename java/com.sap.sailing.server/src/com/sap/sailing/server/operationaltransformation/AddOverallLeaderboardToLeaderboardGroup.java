package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.meta.LeaderboardGroupMetaLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class AddOverallLeaderboardToLeaderboardGroup extends AbstractLeaderboardGroupOperation<Void> {
    private static final long serialVersionUID = -8708216605325043212L;
    
    private final ScoringScheme scoringScheme;
    
    private final int[] discardThresholds;

    public AddOverallLeaderboardToLeaderboardGroup(String leaderboardGroupName, int[] discardThresholds, ScoringScheme scoringScheme) {
        super(leaderboardGroupName);
        this.discardThresholds = discardThresholds;
        this.scoringScheme = scoringScheme;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        LeaderboardGroup leaderboardGroup = toState.getLeaderboardGroupByName(getLeaderboardGroupName());
        if (leaderboardGroup != null) {
            Leaderboard overallLeaderboard = leaderboardGroup.getOverallLeaderboard();
            if (overallLeaderboard == null) {
                overallLeaderboard = new LeaderboardGroupMetaLeaderboard(leaderboardGroup, scoringScheme,
                        new ResultDiscardingRuleImpl(discardThresholds));
                leaderboardGroup.setOverallLeaderboard(overallLeaderboard);
                toState.addLeaderboard(overallLeaderboard);
                toState.updateStoredLeaderboardGroup(leaderboardGroup);
            }
        }
        return null;
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

}
