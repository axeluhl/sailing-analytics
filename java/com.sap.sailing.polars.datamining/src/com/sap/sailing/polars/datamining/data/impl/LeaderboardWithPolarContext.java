package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.polars.datamining.data.HasLeaderboardPolarContext;

public class LeaderboardWithPolarContext implements HasLeaderboardPolarContext {
    
    private final Leaderboard leaderboard;
    private final HasLeaderboardGroupContext leaderboardGroupContext;

    public LeaderboardWithPolarContext(Leaderboard leaderboard, HasLeaderboardGroupContext leaderboardGroupPolarContext) {
        this.leaderboard = leaderboard;
        this.leaderboardGroupContext = leaderboardGroupPolarContext;
    }

    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    @Override
    public HasLeaderboardGroupContext getLeaderboardGroupContext() {
        return leaderboardGroupContext;
    }
    
    @Override
    public BoatClass getBoatClass() {
        return leaderboard.getBoatClass();
    }

}
