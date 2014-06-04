package com.sap.sailing.server.impl;

import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.LeaderboardSearchResult;

public class LeaderboardSearchResultImpl implements LeaderboardSearchResult {
    private final Leaderboard leaderboard;
    
    public LeaderboardSearchResultImpl(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }

    public Regatta getRegatta() {
        final Regatta regatta;
        if (leaderboard instanceof RegattaLeaderboard) {
            regatta = ((RegattaLeaderboard) leaderboard).getRegatta();
        } else {
            regatta = null;
        }
        return regatta;
    }

    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
}
