package com.sap.sailing.windestimation.maneuvergraph.impl.bestpath;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevel;

public class BestPathEvaluationResult<T extends ManeuverNodesLevel<T>> {
    private Map<T, Double> probabilityBonusForLevels = new HashMap<>();

    public void addTackProbabilityBonusForManeuverOfLevel(T maneuverLevel, double tackProbabilityBonus) {
        probabilityBonusForLevels.put(maneuverLevel, tackProbabilityBonus);
    }

    public void reset() {
        probabilityBonusForLevels.clear();
    }

    public void removeTackProbabilityBonusForManeuverOfLevel(T maneuverLevel) {
        probabilityBonusForLevels.remove(maneuverLevel);
    }

    public double getTackProbabilityBonusForManeuverOfLevel(T maneuverLevel) {
        Double tackProbabilityBonus = probabilityBonusForLevels.get(maneuverLevel);
        return tackProbabilityBonus == null ? 0 : tackProbabilityBonus;
    }

    public boolean hasAnyTackProbabilityBonusToOffer() {
        return !probabilityBonusForLevels.isEmpty();
    }

    public void merge(BestPathEvaluationResult<T> otherBestPathEvaluationResult) {
        probabilityBonusForLevels.putAll(otherBestPathEvaluationResult.probabilityBonusForLevels);
    }
}