package com.sap.sailing.windestimation.evaluation;

import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.EstimationDataPersistenceManager;
import com.sap.sailing.windestimation.data.LoggingUtil;
import com.sap.sailing.windestimation.data.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

public class ManeuverSequenceGraphBasedWindEstimatorEvaluationRunner {

    public static void main(String[] args) throws Exception {
        WindEstimatorEvaluator evaluator = new WindEstimationEvaluatorImpl(15, 2, 0.8);
        LoggingUtil.logInfo("Connecting to MongoDB");
        EstimationDataPersistenceManager persistenceManager = new EstimationDataPersistenceManager();
        LoggingUtil.logInfo("Loading estimation data of all races");
        List<RaceWithEstimationData> testSet = persistenceManager.getRacesWithEstimationData();
        LoggingUtil.logInfo("Loading polar data");
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        LoggingUtil.logInfo("Wind estimator evaluation started...");
        WindEstimatorEvaluationResult evaluationResult = evaluator
                .evaluateWindEstimator(new ManeuverSequenceGraphBasedWindEstimatorFactory(polarService), testSet);
        LoggingUtil.logInfo("Wind estimator evaluation finished");
        evaluationResult.printEvaluationStatistics();
    }

}
