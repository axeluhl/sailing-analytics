package com.sap.sailing.windestimation.maneuvergraph.bestpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.GraphLevel;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class MultipleBoatClassBestsPathEvaluator implements BestPathsEvaluator {

    @Override
    public BestPathEvaluationResult evaluateBestPath(List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath) {
        Map<BoatClass, List<Pair<GraphLevel, FineGrainedPointOfSail>>> bestPathPerBoatClass = clusterPathByBoatClass(
                bestPath);
        BestPathEvaluationResult evaluationResult = null;
        SameBoatClassBestPathsEvaluator sameBoatClassBestPathsEvaluator = new SameBoatClassBestPathsEvaluator();
        for (List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPathOfBoatClass : bestPathPerBoatClass.values()) {
            BestPathEvaluationResult otherEvaluationResult = sameBoatClassBestPathsEvaluator
                    .evaluateBestPath(bestPathOfBoatClass);
            if (evaluationResult == null) {
                evaluationResult = otherEvaluationResult;
            } else {
                evaluationResult.merge(otherEvaluationResult);
            }
        }
        return evaluationResult;
    }

    private Map<BoatClass, List<Pair<GraphLevel, FineGrainedPointOfSail>>> clusterPathByBoatClass(
            List<Pair<GraphLevel, FineGrainedPointOfSail>> pathWithVariousBoatClasses) {
        Map<BoatClass, List<Pair<GraphLevel, FineGrainedPointOfSail>>> pathPerBoatClass = new HashMap<>();
        for (Pair<GraphLevel, FineGrainedPointOfSail> pair : pathWithVariousBoatClasses) {
            BoatClass boatClass = pair.getA().getManeuver().getBoatClass();
            List<Pair<GraphLevel, FineGrainedPointOfSail>> pathOfSingleBoatClass = pathPerBoatClass.get(boatClass);
            if (pathOfSingleBoatClass == null) {
                pathOfSingleBoatClass = new ArrayList<>();
                pathPerBoatClass.put(boatClass, pathOfSingleBoatClass);
            }
            pathOfSingleBoatClass.add(pair);
        }
        return pathPerBoatClass;
    }

}
