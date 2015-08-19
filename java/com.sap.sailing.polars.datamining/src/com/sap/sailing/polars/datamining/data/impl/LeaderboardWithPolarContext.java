package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.polars.datamining.data.HasLeaderboardPolarContext;

public class LeaderboardWithPolarContext implements HasLeaderboardPolarContext {
    
    private final Leaderboard leaderboard;

    public LeaderboardWithPolarContext(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

}
