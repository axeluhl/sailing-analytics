package com.sap.sailing.windestimation.evaluation;

import java.lang.reflect.Field;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.persistence.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.data.persistence.RaceWithCompleteManeuverCurvePersistenceManager;
import com.sap.sailing.windestimation.maneuverclassifier.ManeuverFeatures;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class WindEstimatorEvaluationRunner {

    private static final Integer MAX_RACES = null;
    private static final Integer FIXED_NUMBER_OF_MANEUVERS = null;
    private static final boolean RANDOM_CLIPPING_OF_COMPETITOR_TRACKS = false;
    private static final boolean EVALUATE_PER_COMPETITOR_TRACK = false;
    private static final boolean ENABLE_MARKS_INFORMATION = true;
    private static final boolean ENABLE_SCALED_SPEED = true;
    private static final boolean ENABLE_POLARS = true;
    private static final double MIN_CORRECT_ESTIMATIONS_RATIO_FOR_CORRECT_RACE = 0.75;
    private static final double MAX_TWS_DEVIATION_PERCENT = 0.2;
    private static final int MAX_TWD_DEVIATION_DEG = 20;
    private static final EvaluatableWindEstimationImplementation WIND_ESTIMATION_IMPLEMENTATION = EvaluatableWindEstimationImplementation.CLUSTERING;

    public static void main(String[] args) throws Exception {
        WindEstimatorEvaluator<CompleteManeuverCurveWithEstimationData> evaluator = new WindEstimationEvaluatorImpl<>(
                MAX_TWD_DEVIATION_DEG, MAX_TWS_DEVIATION_PERCENT, MIN_CORRECT_ESTIMATIONS_RATIO_FOR_CORRECT_RACE,
                EVALUATE_PER_COMPETITOR_TRACK, FIXED_NUMBER_OF_MANEUVERS == null ? 1 : FIXED_NUMBER_OF_MANEUVERS,
                RANDOM_CLIPPING_OF_COMPETITOR_TRACKS, FIXED_NUMBER_OF_MANEUVERS);
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
                estimatorFactories.get(WIND_ESTIMATION_IMPLEMENTATION),
                new TargetWindFromCompleteManeuverCurveWithEstimationDataExtractor(), racesIterator,
                racesIterator.getNumberOfElements());

        LoggingUtil.logInfo("Wind estimator evaluation finished with the following provided arguments:\r\n"
                + buildConfigurationString() + "\r\n");

        evaluationResult.printEvaluationStatistics(true);
    }

    private static String buildConfigurationString() throws IllegalArgumentException, IllegalAccessException {
        StringBuilder str = new StringBuilder();
        for (Field field : WindEstimatorEvaluationRunner.class.getDeclaredFields()) {
            str.append("\t- ");
            str.append(field.getName());
            str.append(": \t ");
            str.append(field.get(null) + "\r\n");
        }
        return str.toString();
    }

}
