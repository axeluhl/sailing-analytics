package com.sap.sailing.domain.base;

import java.util.List;
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

    private List<String> suppressedCompetitors;

    private Map<String, String> displayNamesByCompetitorId;

    public LeaderboardMasterData(String name, String displayName, int[] resultDiscardingRule,
            Map<String, Competitor> competitorsById, ScoreCorrectionMasterData scoreCorrection,
            Map<String, Double> carriedPoints, List<String> suppressedCompetitors,
            Map<String, String> displayNamesByCompetitorId) {
        this.name = name;
        this.displayName = displayName;
        this.resultDiscardingRule = resultDiscardingRule;
        this.competitorsById = competitorsById;
        this.scoreCorrection = scoreCorrection;
        this.carriedPoints = carriedPoints;
        this.suppressedCompetitors = suppressedCompetitors;
        this.displayNamesByCompetitorId = displayNamesByCompetitorId;
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

    public List<String> getSuppressedCompetitors() {
        return suppressedCompetitors;
    }

    public Map<String, String> getDisplayNamesByCompetitorId() {
        return displayNamesByCompetitorId;
    }

    public abstract Leaderboard getLeaderboard();
    
    
}
