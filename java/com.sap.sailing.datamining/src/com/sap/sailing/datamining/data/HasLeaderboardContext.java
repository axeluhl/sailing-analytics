package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sse.datamining.annotations.Dimension;

public interface HasLeaderboardContext {
    Leaderboard getLeaderboard();

    PolarDataService getPolarDataService();

    @Dimension(messageKey="Leaderboard")
    String getName();
}
