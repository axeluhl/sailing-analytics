package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.polars.PolarDataService;

public interface HasLeaderboardGroupContext {
    
    LeaderboardGroup getLeaderboardGroup();

	PolarDataService getPolarDataService();

}
