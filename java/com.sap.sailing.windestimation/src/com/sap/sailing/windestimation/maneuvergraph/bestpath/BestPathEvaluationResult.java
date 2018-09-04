package com.sap.sailing.windestimation.maneuvergraph.bestpath;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.windestimation.maneuvergraph.GraphLevel;

public class BestPathEvaluationResult {
    private Map<GraphLevel, Double> tackProbabilityBonusForLevels = new HashMap<>();
    private Map<GraphLevel, Double> upwindBeforeProbabilityBonusForLevels = new HashMap<>();
    private Map<GraphLevel, Double> upwindAfterProbabilityBonusForLevels = new HashMap<>();

    public void addTackProbabilityBonusForManeuverOfLevel(GraphLevel maneuverLevel, double tackProbabilityBonus) {
        tackProbabilityBonusForLevels.put(maneuverLevel, tackProbabilityBonus);
    }

    public void addUpwindBeforeProbabilityBonusForManeuverOfLevel(GraphLevel maneuverLevel,
            double upwindBeforeProbabilityBonus) {
        upwindBeforeProbabilityBonusForLevels.put(maneuverLevel, upwindBeforeProbabilityBonus);
    }

    public void addUpwindAfterProbabilityBonusForManeuverOfLevel(GraphLevel maneuverLevel,
            double upwindAfterProbabilityBonus) {
        upwindAfterProbabilityBonusForLevels.put(maneuverLevel, upwindAfterProbabilityBonus);
    }

    public void reset() {
        tackProbabilityBonusForLevels.clear();
        upwindBeforeProbabilityBonusForLevels.clear();
        upwindAfterProbabilityBonusForLevels.clear();
    }

    public void removeProbabilityBonusForManeuverOfLevel(GraphLevel maneuverLevel) {
        tackProbabilityBonusForLevels.remove(maneuverLevel);
        upwindBeforeProbabilityBonusForLevels.remove(maneuverLevel);
        upwindAfterProbabilityBonusForLevels.remove(maneuverLevel);
    }

    public double getTackProbabilityBonusForManeuverOfLevel(GraphLevel maneuverLevel) {
        Double tackProbabilityBonus = tackProbabilityBonusForLevels.get(maneuverLevel);
        return tackProbabilityBonus == null ? 0 : tackProbabilityBonus;
    }

    public double getUpwindBeforeProbabilityBonusForManeuverOfLevel(GraphLevel maneuverLevel) {
        Double probabilityBonus = upwindBeforeProbabilityBonusForLevels.get(maneuverLevel);
        return probabilityBonus == null ? 0 : probabilityBonus;
    }

    public double getUpwindAfterProbabilityBonusForManeuverOfLevel(GraphLevel maneuverLevel) {
        Double probabilityBonus = upwindAfterProbabilityBonusForLevels.get(maneuverLevel);
        return probabilityBonus == null ? 0 : probabilityBonus;
    }

    public boolean hasAnyProbabilityBonusToOffer() {
        return !tackProbabilityBonusForLevels.isEmpty() || !upwindBeforeProbabilityBonusForLevels.isEmpty()
                || !upwindAfterProbabilityBonusForLevels.isEmpty();
    }

    public void merge(BestPathEvaluationResult otherBestPathEvaluationResult) {
        tackProbabilityBonusForLevels.putAll(otherBestPathEvaluationResult.tackProbabilityBonusForLevels);
        upwindBeforeProbabilityBonusForLevels
                .putAll(otherBestPathEvaluationResult.upwindBeforeProbabilityBonusForLevels);
        upwindAfterProbabilityBonusForLevels.putAll(otherBestPathEvaluationResult.upwindAfterProbabilityBonusForLevels);
    }
}