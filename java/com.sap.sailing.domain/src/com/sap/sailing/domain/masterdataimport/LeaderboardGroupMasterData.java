package com.sap.sailing.domain.masterdataimport;

import java.util.Set;

import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.leaderboard.ScoringScheme;

/**
 * Holds leaderboard group object AND its leaderboards, etc
 * @author d054528
 *
 */
public class LeaderboardGroupMasterData {

    private final String name;
    private final String description;
    private final boolean hasOverallLeaderboard;
    private final ScoringScheme overallLeaderboardScoringScheme;
    private final int[] overallLeaderboardDiscardingRule;
    private final Iterable<LeaderboardMasterData> leaderboards;
    private final boolean displayGroupsRevese;
    private final Set<EventMasterData> events;
    private final Set<RegattaMasterData> regattas;


    public LeaderboardGroupMasterData(String name, String description, boolean displayGroupsRevese,
            boolean hasOverallLeaderboard,
            ScoringScheme overallLeaderboardScoringScheme,
            int[] overallLeaderboardDiscardingRule, Iterable<LeaderboardMasterData> leaderboards, Set<EventMasterData> events,
            Set<RegattaMasterData> regattas) {
        super();
        this.name = name;
        this.description = description;
        this.hasOverallLeaderboard = hasOverallLeaderboard;
        this.overallLeaderboardScoringScheme = overallLeaderboardScoringScheme;
        this.overallLeaderboardDiscardingRule = overallLeaderboardDiscardingRule;
        this.leaderboards = leaderboards;
        this.displayGroupsRevese = displayGroupsRevese;
        this.events = events;
        this.regattas = regattas;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasOverallLeaderboard() {
        return hasOverallLeaderboard;
    }

    public ScoringScheme getOverallLeaderboardScoringScheme() {
        return overallLeaderboardScoringScheme;
    }

    public int[] getOverallLeaderboardDiscardingRule() {
        return overallLeaderboardDiscardingRule;
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
