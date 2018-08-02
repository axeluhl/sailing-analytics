package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.leaderboard.Leaderboard;

public class LeaderboardWithContext implements HasLeaderboardContext {
    private final Leaderboard leaderboard;
    private final HasLeaderboardGroupContext leaderboardGroupContext;

    public LeaderboardWithContext(Leaderboard leaderboard, HasLeaderboardGroupContext leaderboardGroupContext) {
        this.leaderboard = leaderboard;
        this.leaderboardGroupContext = leaderboardGroupContext;
    }

    public HasLeaderboardGroupContext getLeaderboardGroupContext() {
        return leaderboardGroupContext;
    }
    
    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
    
    @Override
    public BoatClass getBoatClass() {
        return getLeaderboard().getBoatClass();
    }

    @Override
    public String getName() {
        return getLeaderboard().getName();
    }
}
