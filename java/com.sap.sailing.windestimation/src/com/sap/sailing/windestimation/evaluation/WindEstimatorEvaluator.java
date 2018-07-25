package com.sap.sailing.windestimation.evaluation;

import java.util.Iterator;

import com.sap.sailing.windestimation.ManeuverAndPolarsBasedWindEstimator;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface WindEstimatorEvaluator<T> {

    WindEstimatorEvaluationResult evaluateWindEstimator(ManeuverAndPolarsBasedWindEstimatorFactory windEstimatorFactory,
            Iterator<RaceWithEstimationData<T>> racesIterator, long numberOfRaces);

    WindEstimatorEvaluationResult evaluateWindEstimator(ManeuverAndPolarsBasedWindEstimator windEstimator,
            RaceWithEstimationData<T> race);

}
