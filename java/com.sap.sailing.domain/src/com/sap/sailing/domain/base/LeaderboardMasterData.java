package com.sap.sailing.domain.base;

import java.util.Map;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.masterdataimport.ScoreCorrectionMasterData;

public abstract class LeaderboardMasterData {
    
    private String name;
    
    private String displayName;
    
    private int[] resultDiscardingRule;
    
    private Map<String,Competitor>  competitorsById;

    private ScoreCorrectionMasterData scoreCorrection;

    private Map<String, Double> carriedPoints;

    public LeaderboardMasterData(String name, String displayName, int[] resultDiscardingRule,
            Map<String,Competitor> competitorsById, ScoreCorrectionMasterData scoreCorrection, Map<String, Double> carriedPoints) {
        this.name = name;
        this.displayName = displayName;
        this.resultDiscardingRule = resultDiscardingRule;
        this.competitorsById = competitorsById;
        this.scoreCorrection = scoreCorrection;
        this.carriedPoints = carriedPoints;
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

    public Map<String,Competitor> getCompetitorsById() {
        return competitorsById;
    }

    public ScoreCorrectionMasterData getScoreCorrection() {
        return scoreCorrection;
    }

    public Map<String, Double> getCarriedPoints() {
        return carriedPoints;
    }

    public abstract Leaderboard getLeaderboard();
    
    
}
