package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.polars.PolarDataService;

public class LeaderboardGroupWithContext implements HasLeaderboardGroupContext {
    private final LeaderboardGroup leaderboardGroup;
    private final PolarDataService polarDataService;

    public LeaderboardGroupWithContext(LeaderboardGroup leaderboardGroup, PolarDataService polarDataService) {
        this.leaderboardGroup = leaderboardGroup;
        this.polarDataService = polarDataService;
    }

    @Override
    public LeaderboardGroup getLeaderboardGroup() {
        return leaderboardGroup;
    }

    @Override
    public PolarDataService getPolarDataService() {
        return polarDataService;
    }
}
