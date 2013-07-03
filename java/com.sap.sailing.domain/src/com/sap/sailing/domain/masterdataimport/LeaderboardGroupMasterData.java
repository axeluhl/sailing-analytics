package com.sap.sailing.domain.masterdataimport;

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
    private Iterable<LeaderboardMasterData> leaderboards;
    private boolean displayGroupsRevese;
    private Set<EventMasterData> events;
    private Set<RegattaMasterData> regattas;

    public LeaderboardGroupMasterData(String name, String description,
            boolean displayGroupsRevese, LeaderboardMasterData overallLeaderboardMasterData, Iterable<LeaderboardMasterData> leaderboards, Set<EventMasterData> events, Set<RegattaMasterData> regattas) {
                this.name = name;
                this.description = description;
                this.displayGroupsRevese = displayGroupsRevese;
                this.overallLeaderboardMasterData = overallLeaderboardMasterData;
                this.leaderboards = leaderboards;
                this.events = events;
                this.regattas = regattas;
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

    public Iterable<LeaderboardMasterData> getLeaderboards() {
        return leaderboards;
    }

    public boolean isDisplayGroupsRevese() {
        return displayGroupsRevese;
    }

    public Set<EventMasterData> getEvents() {
        return events;
    }

    public Set<RegattaMasterData> getRegattas() {
        return regattas;
    }
    

}
