package com.sap.sailing.windestimation.evaluation;

import java.util.function.Function;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.persistence.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.data.persistence.RaceWithCompleteManeuverCurvePersistenceManager;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;
import com.sap.sailing.windestimation.maneuvergraph.BestPathCalculatorForConfidenceEvaluation;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class WindEstimatorEvaluationRunner {

    private static final Integer MAX_RACES = null;
    private static final boolean ENABLE_MARKS_INFORMATION = true;
    private static final boolean ENABLE_SCALED_SPEED = true;
    private static final boolean ENABLE_POLARS = true;
    private static final double MIN_CORRECT_ESTIMATIONS_RATIO_FOR_CORRECT_RACE = 0.75;
    private static final int MAX_TWS_DEVIATION_KNOTS = 2;
    private static final int MAX_TWD_DEVIATION_DEG = 20;
    private static final Function<WindEstimatorFactories, WindEstimatorFactory<CompleteManeuverCurveWithEstimationData>> estimatorFactoryRetriever = factories -> factories
            .maneuverGraph();

    public static void main(String[] args) throws Exception {
        WindEstimatorEvaluator<CompleteManeuverCurveWithEstimationData> evaluator = new WindEstimationEvaluatorImpl<>(
                MAX_TWD_DEVIATION_DEG, MAX_TWS_DEVIATION_KNOTS, MIN_CORRECT_ESTIMATIONS_RATIO_FOR_CORRECT_RACE);
        LoggingUtil.logInfo("Connecting to MongoDB");
        RaceWithCompleteManeuverCurvePersistenceManager persistenceManager = new RaceWithCompleteManeuverCurvePersistenceManager();
        LoggingUtil.logInfo("Loading polar data");
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        LoggingUtil.logInfo("Wind estimator evaluation started...");
        PersistedElementsIterator<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> racesIterator = persistenceManager
                .getIterator(persistenceManager.getFilterQueryForYear(2018, false));
        if (MAX_RACES != null) {
            racesIterator = racesIterator.limit(MAX_RACES);
        }
        WindEstimatorFactories estimatorFactories = new WindEstimatorFactories(polarService,
                new ManeuverFeatures(ENABLE_POLARS, ENABLE_SCALED_SPEED, ENABLE_MARKS_INFORMATION));

        WindEstimatorEvaluationResult evaluationResult = evaluator.evaluateWindEstimator(
                estimatorFactoryRetriever.apply(estimatorFactories),
                new TargetWindFromCompleteManeuverCurveWithEstimationDataExtractor(), racesIterator,
                racesIterator.getNumberOfElements());
        LoggingUtil.logInfo("Wind estimator evaluation finished");
        BestPathCalculatorForConfidenceEvaluation.toCsv();
        evaluationResult.printEvaluationStatistics(true);
    }

}
