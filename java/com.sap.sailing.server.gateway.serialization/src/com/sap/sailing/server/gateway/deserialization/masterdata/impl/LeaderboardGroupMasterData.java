package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.Set;

import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.base.impl.EventMasterData;

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
    private Set<EventMasterData> events;

    public LeaderboardGroupMasterData(String name, String description,
            boolean displayGroupsRevese, LeaderboardMasterData overallLeaderboardMasterData, Set<LeaderboardMasterData> leaderboards, Set<EventMasterData> events) {
                this.name = name;
                this.description = description;
                this.displayGroupsRevese = displayGroupsRevese;
                this.overallLeaderboardMasterData = overallLeaderboardMasterData;
                this.leaderboards = leaderboards;
                this.events = events;
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

    public Set<EventMasterData> getEvents() {
        return events;
    }
    
    
    
    

}
