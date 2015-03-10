package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.domain.leaderboard.Leaderboard;

public class LeaderboardWithContext implements HasLeaderboardContext {

    private final Leaderboard leaderboard;

    public LeaderboardWithContext(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

}
