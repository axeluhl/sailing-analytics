package com.sap.sailing.windestimation.evaluation;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.EstimationDataPersistenceManager;
import com.sap.sailing.windestimation.data.LoggingUtil;
import com.sap.sailing.windestimation.data.PersistedRacesWithEstimationDataIterator;
import com.sap.sailing.windestimation.data.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.maneuvergraph.TargetWindFromCompleteManeuverCurveWithEstimationDataExtractor;

public class ManeuverSequenceGraphBasedWindEstimatorEvaluationRunner {

    public static void main(String[] args) throws Exception {
        WindEstimatorEvaluator<CompleteManeuverCurveWithEstimationData> evaluator = new WindEstimationEvaluatorImpl<>(
                15, 2, 0.8);
        LoggingUtil.logInfo("Connecting to MongoDB");
        EstimationDataPersistenceManager persistenceManager = new EstimationDataPersistenceManager();
        LoggingUtil.logInfo("Loading polar data");
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        LoggingUtil.logInfo("Wind estimator evaluation started...");
        PersistedRacesWithEstimationDataIterator<CompleteManeuverCurveWithEstimationData> racesIterator = new PersistedRacesWithEstimationDataIterator<>(
                persistenceManager);

        WindEstimatorEvaluationResult evaluationResult = evaluator.evaluateWindEstimator(
                new ManeuverSequenceGraphBasedWindEstimatorFactory(polarService),
                new TargetWindFromCompleteManeuverCurveWithEstimationDataExtractor(), racesIterator,
                racesIterator.getNumberOfRaces());
        LoggingUtil.logInfo("Wind estimator evaluation finished");
        evaluationResult.printEvaluationStatistics(true);
    }

}
