package com.sap.sailing.server.impl;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;

public class LeaderboardSearchResultImpl implements LeaderboardSearchResult {
    private final Leaderboard leaderboard;
    private final EventBase event;
    
    public LeaderboardSearchResultImpl(Leaderboard leaderboard, EventBase event) {
        this.leaderboard = leaderboard;
        this.event = event;
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

    @Override
    public EventBase getEvent() {
        return event;
    }
}
