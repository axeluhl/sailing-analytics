package com.sap.sailing.server.operationaltransformation;

import java.util.Arrays;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateLeaderboard extends AbstractLeaderboardOperation<Void> {
    private static final long serialVersionUID = -8040361040050151768L;
    private final String newLeaderboardName;
    private final String newLeaderboardDisplayName;
    private final int[] newDiscardingThresholds;
    
    public UpdateLeaderboard(String leaderboardName, String newLeaderboardName, String newLeaderboardDisplayName, int[] newDiscardingThresholds) {
        super(leaderboardName);
        this.newLeaderboardName = newLeaderboardName;
        this.newLeaderboardDisplayName = newLeaderboardDisplayName;
        this.newDiscardingThresholds = newDiscardingThresholds;
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
        if (!getLeaderboardName().equals(newLeaderboardName)) {
            toState.renameLeaderboard(getLeaderboardName(), newLeaderboardName);
        }
        Leaderboard leaderboard = toState.getLeaderboardByName(newLeaderboardName);
        if (!Arrays.equals(leaderboard.getResultDiscardingRule().getDiscardIndexResultsStartingWithHowManyRaces(), newDiscardingThresholds)) {
            leaderboard.setResultDiscardingRule(new ResultDiscardingRuleImpl(newDiscardingThresholds));
        }
        leaderboard.setDisplayName(newLeaderboardDisplayName);
        updateStoredLeaderboard(toState, leaderboard);
        return null;
    }

}
