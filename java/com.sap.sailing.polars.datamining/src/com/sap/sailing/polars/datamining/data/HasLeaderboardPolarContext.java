package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.datamining.annotations.Connector;

public interface HasLeaderboardPolarContext {
    
    @Connector(messageKey="Leaderboard")
    Leaderboard getLeaderboard();
    
    @Connector(scanForStatistics=false)
    HasLeaderboardGroupPolarContext getLeaderboardGroupPolarContext();

}
