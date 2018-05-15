package com.sap.sailing.windestimation.evaluation;

import java.util.List;

import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.EstimationDataPersistenceManager;
import com.sap.sailing.windestimation.data.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;

public class ManeuverSequenceGraphBasedWindEstimatorEvaluationRunner {

    public static void main(String[] args) throws Exception {
        WindEstimatorEvaluator evaluator = new WindEstimationEvaluatorImpl(15, 2, 0.8);
        EstimationDataPersistenceManager persistenceManager = new EstimationDataPersistenceManager();
        List<RaceWithEstimationData> testSet = persistenceManager.getRacesWithEstimationData();
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        WindEstimatorEvaluationResult evaluationResult = evaluator
                .evaluateWindEstimator(new ManeuverSequenceGraphBasedWindEstimatorFactory(polarService), testSet);
        evaluationResult.printEvaluationStatistics();
    }

}
