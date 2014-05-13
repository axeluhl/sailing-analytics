package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public class HasLeaderboardContextImpl extends HasLeaderboardGroupContextImpl implements HasLeaderboardContext {

    private final Leaderboard leaderboard;

    public HasLeaderboardContextImpl(LeaderboardGroup leaderboardGroup, Leaderboard leaderboard) {
        super(leaderboardGroup);
        this.leaderboard = leaderboard;
    }

    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

}