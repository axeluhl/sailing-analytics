package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.persistence.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.data.persistence.RaceWithCompleteManeuverCurvePersistenceManager;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class ManeuverSequenceGraphBasedWindEstimatorEvaluationRunner {

    public static void main(String[] args) throws Exception {
        WindEstimatorEvaluator<CompleteManeuverCurveWithEstimationData> evaluator = new WindEstimationEvaluatorImpl<>(
                30, 2, 0.8);
        LoggingUtil.logInfo("Connecting to MongoDB");
        RaceWithCompleteManeuverCurvePersistenceManager persistenceManager = new RaceWithCompleteManeuverCurvePersistenceManager();
        LoggingUtil.logInfo("Loading polar data");
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        LoggingUtil.logInfo("Wind estimator evaluation started...");
        PersistedElementsIterator<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> racesIterator = persistenceManager
                .getIterator();
        WindEstimatorFactories estimatorFactories = new WindEstimatorFactories(polarService);

        WindEstimatorEvaluationResult evaluationResult = evaluator.evaluateWindEstimator(
                estimatorFactories.maneuverGraph(),
                new TargetWindFromCompleteManeuverCurveWithEstimationDataExtractor(), racesIterator,
                racesIterator.getNumberOfElements());
        LoggingUtil.logInfo("Wind estimator evaluation finished");
        evaluationResult.printEvaluationStatistics(true);
    }

}
