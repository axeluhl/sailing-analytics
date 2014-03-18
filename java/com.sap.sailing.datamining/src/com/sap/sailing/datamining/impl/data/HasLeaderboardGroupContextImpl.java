package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public class HasLeaderboardGroupContextImpl implements HasLeaderboardGroupContext {

    private final LeaderboardGroup leaderboardGroup;

    public HasLeaderboardGroupContextImpl(LeaderboardGroup leaderboardGroup) {
        this.leaderboardGroup = leaderboardGroup;
    }

    @Override
    public LeaderboardGroup getLeaderboardGroup() {
        return leaderboardGroup;
    }

}