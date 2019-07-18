package com.sap.sailing.windestimation.evaluation;

import java.util.Iterator;

import com.sap.sailing.windestimation.data.RaceWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface WindEstimationEvaluator<T> {

    WindEstimatorEvaluationResult evaluateWindEstimator(
            WindEstimatorFactory<RaceWithEstimationData<T>> windEstimatorFactory,
            TargetWindFixesExtractor<T> targetWindFixesExtractor, Iterator<RaceWithEstimationData<T>> racesIterator,
            long numberOfRaces);

    WindEstimatorEvaluationResult evaluateWindEstimator(EvaluationCase<T> evaluationCase);

}
