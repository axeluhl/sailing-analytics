package com.sap.sailing.domain.base;

import java.util.Set;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;

public abstract class LeaderboardMasterData {
    
    private String name;
    
    private String displayName;
    
    private int[] resultDiscardingRule;
    
    private Set<Competitor> competitors;

    public LeaderboardMasterData(String name, String displayName, int[] resultDiscardingRule,
            Set<Competitor> competitors) {
        this.name = name;
        this.displayName = displayName;
        this.resultDiscardingRule = resultDiscardingRule;
        this.competitors = competitors;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ThresholdBasedResultDiscardingRule getResultDiscardingRule() {
        if (resultDiscardingRule == null) {
            return null;
        }
        ThresholdBasedResultDiscardingRule rule = new ThresholdBasedResultDiscardingRuleImpl(resultDiscardingRule);
        return rule;
    }

    public Set<Competitor> getCompetitors() {
        return competitors;
    }

    public abstract Leaderboard getLeaderboard();
    
    
}
