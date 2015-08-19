package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.datamining.shared.annotations.Connector;

public interface HasLeaderboardPolarContext {
    
    @Connector(messageKey="Leaderboard")
    Leaderboard getLeaderboard();

}
