package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.Set;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

/**
 * Holds leaderboard group object AND its leaderboards, etc
 * @author d054528
 *
 */
public class LeaderboardGroupMasterData {
    
    private LeaderboardGroup leaderboardGroup;
    
    private Set<Leaderboard> leaderboards;

    public Set<Leaderboard> getLeaderboards() {
        return leaderboards;
    }

}
