package com.sap.sailing.server.impl;

import java.util.Collections;
import java.util.Set;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;

public class LeaderboardSearchResultImpl implements LeaderboardSearchResult {
    private final Leaderboard leaderboard;
    private final EventBase event;
    private final Set<LeaderboardGroup> leaderboardGroups;
    
    public LeaderboardSearchResultImpl(Leaderboard leaderboard, EventBase event, Set<LeaderboardGroup> leaderboardGroups) {
        this.leaderboard = leaderboard;
        if (leaderboardGroups == null) {
            this.leaderboardGroups = Collections.emptySet();
        } else {
            this.leaderboardGroups = leaderboardGroups;
        }
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

    @Override
    public Set<LeaderboardGroup> getLeaderboardGroups() {
        return leaderboardGroups;
    }
}
