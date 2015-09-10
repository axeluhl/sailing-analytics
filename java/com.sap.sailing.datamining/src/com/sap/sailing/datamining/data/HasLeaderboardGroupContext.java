package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sse.datamining.shared.annotations.Connector;

public interface HasLeaderboardGroupContext {
    
    @Connector(messageKey="LeaderboardGroup", ordinal=0)
    LeaderboardGroup getLeaderboardGroup();

    PolarDataService getPolarDataService();

}
