package com.sap.sailing.domain.base;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.masterdataimport.ScoreCorrectionMasterData;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public abstract class LeaderboardMasterData {

    private String name;

    private String displayName;

    private int[] resultDiscardingRule;

    private Map<Serializable, Competitor> competitorsById;

    private ScoreCorrectionMasterData scoreCorrection;

    private Map<Serializable, Double> carriedPoints;

    private List<Serializable> suppressedCompetitors;

    private Map<Serializable, String> displayNamesByCompetitorId;

    private Map<String, Map<String, List<RaceLogEvent>>> raceLogEvents;

    public LeaderboardMasterData(String name, String displayName, int[] resultDiscardingRule,
            Map<Serializable, Competitor> competitorsById, ScoreCorrectionMasterData scoreCorrection,
            Map<Serializable, Double> carriedPoints, List<Serializable> suppressedCompetitors,
            Map<Serializable, String> displayNamesByCompetitorId,
            Map<String, Map<String, List<RaceLogEvent>>> raceLogEvents) {
        this.name = name;
        this.displayName = displayName;
        this.resultDiscardingRule = resultDiscardingRule;
        this.competitorsById = competitorsById;
        this.scoreCorrection = scoreCorrection;
        this.carriedPoints = carriedPoints;
        this.suppressedCompetitors = suppressedCompetitors;
        this.displayNamesByCompetitorId = displayNamesByCompetitorId;
        this.raceLogEvents = raceLogEvents;
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

    public Map<Serializable, Competitor> getCompetitorsById() {
        return competitorsById;
    }

    public ScoreCorrectionMasterData getScoreCorrection() {
        return scoreCorrection;
    }

    public Map<Serializable, Double> getCarriedPoints() {
        return carriedPoints;
    }

    public List<Serializable> getSuppressedCompetitors() {
        return suppressedCompetitors;
    }

    public Map<Serializable, String> getDisplayNamesByCompetitorId() {
        return displayNamesByCompetitorId;
    }

    public Map<String, Map<String, List<RaceLogEvent>>> getRaceLogEvents() {
        return raceLogEvents;
    }

    public abstract Leaderboard getLeaderboard();

}
