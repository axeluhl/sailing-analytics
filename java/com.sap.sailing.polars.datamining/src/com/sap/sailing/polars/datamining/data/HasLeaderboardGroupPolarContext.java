package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sse.datamining.annotations.Connector;

public interface HasLeaderboardGroupPolarContext {
    
    @Connector(messageKey="LeaderboardGroup")
    LeaderboardGroup getLeaderboardGroup();

}
