package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.polars.datamining.data.HasLeaderboardGroupPolarContext;
import com.sap.sailing.polars.datamining.data.HasLeaderboardPolarContext;

public class LeaderboardWithPolarContext implements HasLeaderboardPolarContext {
    
    private final Leaderboard leaderboard;
    private final HasLeaderboardGroupPolarContext leaderboardGroupPolarContext;

    public LeaderboardWithPolarContext(Leaderboard leaderboard, HasLeaderboardGroupPolarContext leaderboardGroupPolarContext) {
        this.leaderboard = leaderboard;
        this.leaderboardGroupPolarContext = leaderboardGroupPolarContext;
    }

    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    @Override
    public HasLeaderboardGroupPolarContext getLeaderboardGroupPolarContext() {
        return leaderboardGroupPolarContext;
    }
    
    @Override
    public BoatClass getBoatClass() {
        return leaderboard.getBoatClass();
    }

}
