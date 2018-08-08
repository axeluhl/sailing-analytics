package com.sap.sailing.windestimation.maneuvergraph.impl.bestpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevel;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <T>
 */
public class MultipleBoatClassBestsPathEvaluator<T extends ManeuverNodesLevel<T>> implements BestPathsEvaluator<T> {

    @Override
    public BestPathEvaluationResult<T> evaluateBestPath(List<Pair<T, FineGrainedPointOfSail>> bestPath) {
        Map<BoatClass, List<Pair<T, FineGrainedPointOfSail>>> bestPathPerBoatClass = clusterPathByBoatClass(bestPath);
        BestPathEvaluationResult<T> evaluationResult = null;
        SameBoatClassBestPathsEvaluator<T> sameBoatClassBestPathsEvaluator = new SameBoatClassBestPathsEvaluator<>();
        for (List<Pair<T, FineGrainedPointOfSail>> bestPathOfBoatClass : bestPathPerBoatClass.values()) {
            BestPathEvaluationResult<T> otherEvaluationResult = sameBoatClassBestPathsEvaluator
                    .evaluateBestPath(bestPathOfBoatClass);
            if (evaluationResult == null) {
                evaluationResult = otherEvaluationResult;
            } else {
                evaluationResult.merge(otherEvaluationResult);
            }
        }
        return evaluationResult;
    }

    private Map<BoatClass, List<Pair<T, FineGrainedPointOfSail>>> clusterPathByBoatClass(
            List<Pair<T, FineGrainedPointOfSail>> pathWithVariousBoatClasses) {
        Map<BoatClass, List<Pair<T, FineGrainedPointOfSail>>> pathPerBoatClass = new HashMap<>();
        for (Pair<T, FineGrainedPointOfSail> pair : pathWithVariousBoatClasses) {
            BoatClass boatClass = pair.getA().getBoatClass();
            List<Pair<T, FineGrainedPointOfSail>> pathOfSingleBoatClass = pathPerBoatClass.get(boatClass);
            if (pathOfSingleBoatClass == null) {
                pathOfSingleBoatClass = new ArrayList<>();
                pathPerBoatClass.put(boatClass, pathOfSingleBoatClass);
            }
            pathOfSingleBoatClass.add(pair);
        }
        return pathPerBoatClass;
    }

}
