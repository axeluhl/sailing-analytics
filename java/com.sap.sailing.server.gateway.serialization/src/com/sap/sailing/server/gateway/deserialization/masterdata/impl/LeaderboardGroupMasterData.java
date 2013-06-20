package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.Set;

import com.sap.sailing.domain.base.LeaderboardMasterData;

/**
 * Holds leaderboard group object AND its leaderboards, etc
 * @author d054528
 *
 */
public class LeaderboardGroupMasterData {

    private String name;
    private String description;
    private LeaderboardMasterData overallLeaderboardMasterData;
    private Set<LeaderboardMasterData> leaderboards;
    private boolean displayGroupsRevese;

    public LeaderboardGroupMasterData(String name, String description,
            boolean displayGroupsRevese, LeaderboardMasterData overallLeaderboardMasterData, Set<LeaderboardMasterData> leaderboards) {
                this.name = name;
                this.description = description;
                this.displayGroupsRevese = displayGroupsRevese;
                this.overallLeaderboardMasterData = overallLeaderboardMasterData;
                this.leaderboards = leaderboards;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LeaderboardMasterData getOverallLeaderboardMasterData() {
        return overallLeaderboardMasterData;
    }

    public Set<LeaderboardMasterData> getLeaderboards() {
        return leaderboards;
    }

    public boolean isDisplayGroupsRevese() {
        return displayGroupsRevese;
    }
    
    
    
    

}
