package com.sap.sailing.windestimation.evaluation;

import java.util.List;

import com.sap.sailing.windestimation.data.RaceWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface WindEstimatorEvaluator {

    WindEstimatorEvaluationResult evaluateWindEstimator(
            ManeuverAndPolarsBasedWindEstimatorFactory windEstimatorFactory, List<RaceWithEstimationData> testSet);

}
