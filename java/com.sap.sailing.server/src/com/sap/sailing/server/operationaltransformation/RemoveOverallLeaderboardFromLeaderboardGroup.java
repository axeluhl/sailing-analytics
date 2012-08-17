package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class RemoveOverallLeaderboardFromLeaderboardGroup extends AbstractLeaderboardGroupOperation<Void> {
    private static final long serialVersionUID = -8708216605325043212L;
    
    public RemoveOverallLeaderboardFromLeaderboardGroup(String leaderboardGroupName) {
        super(leaderboardGroupName);
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        LeaderboardGroup leaderboardGroup = toState.getLeaderboardGroupByName(getLeaderboardGroupName());
        if (leaderboardGroup != null) {
            leaderboardGroup.setOverallLeaderboard(null);
            toState.updateStoredLeaderboardGroup(leaderboardGroup);
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
