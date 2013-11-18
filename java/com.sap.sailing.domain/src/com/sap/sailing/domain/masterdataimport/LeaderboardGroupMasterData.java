package com.sap.sailing.domain.masterdataimport;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;

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
    private final Map<String, Double> metaColumnsWithFactors;
    private final List<String> overallLeaderboardSuppressedCompetitorIds;

    public LeaderboardGroupMasterData(String name, String description, boolean displayGroupsRevese,
            boolean hasOverallLeaderboard, ScoringScheme overallLeaderboardScoringScheme,
            int[] overallLeaderboardDiscardingRule, List<String> overallLeaderboardAuppressedCompetitorIds,
            Map<String, Double> metaColumnsWithFactors, Iterable<LeaderboardMasterData> leaderboards, Set<EventMasterData> events, Set<RegattaMasterData> regattas) {
        super();
        this.name = name;
        this.description = description;
        this.hasOverallLeaderboard = hasOverallLeaderboard;
        this.overallLeaderboardScoringScheme = overallLeaderboardScoringScheme;
        this.overallLeaderboardSuppressedCompetitorIds = overallLeaderboardAuppressedCompetitorIds;
        this.overallLeaderboardDiscardingRule = overallLeaderboardDiscardingRule;
        this.metaColumnsWithFactors = metaColumnsWithFactors;
        this.leaderboards = leaderboards;
        this.displayGroupsRevese = displayGroupsRevese;
        this.events = events;
        this.regattas = regattas;
    }
    
    public Competitor getCompetitorById(String competitorId) {
        for (LeaderboardMasterData leaderboard : leaderboards) {
            Competitor c = leaderboard.getCompetitorsById().get(competitorId);
            if (c != null) {
                return c;
            }
        }
        return null;
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

    public List<String> getOverallLeaderboardSuppressedCompetitorIds() {
        return overallLeaderboardSuppressedCompetitorIds;
    }

    public ThresholdBasedResultDiscardingRule getOverallLeaderboardDiscardingRule() {
        return overallLeaderboardDiscardingRule == null ? new ThresholdBasedResultDiscardingRuleImpl(new int[0]) :
            new ThresholdBasedResultDiscardingRuleImpl(overallLeaderboardDiscardingRule);
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

    public Map<String, Double> getMetaColumnsWithFactors() {
        return metaColumnsWithFactors;
    }
    

}
