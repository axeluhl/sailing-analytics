package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.polars.PolarDataService;

public interface HasLeaderboardContext {
    
    Leaderboard getLeaderboard();

	PolarDataService getPolarDataService();

}
