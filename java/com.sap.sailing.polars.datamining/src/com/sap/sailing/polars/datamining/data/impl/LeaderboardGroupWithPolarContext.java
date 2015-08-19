package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.polars.datamining.data.HasLeaderboardGroupPolarContext;

public class LeaderboardGroupWithPolarContext implements HasLeaderboardGroupPolarContext {
    
    private final LeaderboardGroup leaderboardGroup;
    
    public LeaderboardGroupWithPolarContext(LeaderboardGroup leaderboardGroup) {
        this.leaderboardGroup = leaderboardGroup;
    }

    @Override
    public LeaderboardGroup getLeaderboardGroup() {
        return leaderboardGroup;
    }

}
