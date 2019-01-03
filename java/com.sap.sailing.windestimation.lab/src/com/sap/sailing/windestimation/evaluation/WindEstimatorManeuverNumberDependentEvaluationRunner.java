package com.sap.sailing.windestimation.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.RaceWithEstimationData;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.maneuver.RaceWithCompleteManeuverCurvePersistenceManager;
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class WindEstimatorManeuverNumberDependentEvaluationRunner {

    private static final Integer MAX_RACES = null;
    private static final int MAX_MANEUVERS = 10;
    private static final boolean EVALUATE_PER_COMPETITOR_TRACK = true;
    private static final boolean ENABLE_MARKS_INFORMATION = false;
    private static final boolean ENABLE_SCALED_SPEED = false;
    private static final boolean ENABLE_POLARS = true;
    private static final double MIN_CORRECT_ESTIMATIONS_RATIO_FOR_CORRECT_RACE = 0.75;
    private static final double MAX_TWS_DEVIATION_PERCENT = 0.2;
    private static final int MAX_TWD_DEVIATION_DEG = 20;
    private static final EvaluatableWindEstimationImplementation WIND_ESTIMATION_IMPLEMENTATION = EvaluatableWindEstimationImplementation.HMM;

    private static final File csvFile = new File(
            "maneuverNumberDependentEvaluation" + WIND_ESTIMATION_IMPLEMENTATION + ".csv");

    public static void main(String[] args) throws Exception {
        LoggingUtil.logInfo("Connecting to MongoDB");
        RaceWithCompleteManeuverCurvePersistenceManager persistenceManager = new RaceWithCompleteManeuverCurvePersistenceManager();
        LoggingUtil.logInfo("Loading polar data");
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        LoggingUtil.logInfo("Wind estimator evaluation started...");
        ModelStore classifierModelStore = new MongoDbModelStore(persistenceManager.getDb());
        WindEstimatorFactories estimatorFactories = new WindEstimatorFactories(polarService,
                new ManeuverFeatures(ENABLE_POLARS, ENABLE_SCALED_SPEED, ENABLE_MARKS_INFORMATION),
                classifierModelStore);

        double[] avgErrorDegreesPerManeuverCount = new double[MAX_MANEUVERS];
        double[] avgConfidencePerManeuverCount = new double[MAX_MANEUVERS];
        double[] avgConfidenceOfCorrectEstimationsPerManeuverCount = new double[MAX_MANEUVERS];
        double[] avgConfidenceOfIncorrectEstimationsPerManeuverCount = new double[MAX_MANEUVERS];
        double[] accuracyPerManeuverCount = new double[MAX_MANEUVERS];
        double[] emptyEstimationsPercentagePerManeuverCount = new double[MAX_MANEUVERS];

        for (int fixedNumberOfManeuvers = 1; fixedNumberOfManeuvers <= MAX_MANEUVERS; fixedNumberOfManeuvers++) {
            LoggingUtil.logInfo("Running evaluation with " + fixedNumberOfManeuvers + " maneuvers");
            WindEstimationEvaluator<CompleteManeuverCurveWithEstimationData> evaluator = new WindEstimationEvaluatorImpl<>(
                    MAX_TWD_DEVIATION_DEG, MAX_TWS_DEVIATION_PERCENT, MIN_CORRECT_ESTIMATIONS_RATIO_FOR_CORRECT_RACE,
                    EVALUATE_PER_COMPETITOR_TRACK, MAX_MANEUVERS, true, fixedNumberOfManeuvers);
            PersistedElementsIterator<RaceWithEstimationData<CompleteManeuverCurveWithEstimationData>> racesIterator = persistenceManager
                    .getIterator(persistenceManager.getFilterQueryForYear(2018, false));
            if (MAX_RACES != null) {
                racesIterator = racesIterator.limit(MAX_RACES);
            }
            WindEstimatorEvaluationResult evaluationResult = evaluator.evaluateWindEstimator(
                    estimatorFactories.get(WIND_ESTIMATION_IMPLEMENTATION),
                    new TargetWindFromCompleteManeuverCurveWithEstimationDataExtractor(), racesIterator,
                    racesIterator.getNumberOfElements());
            int i = fixedNumberOfManeuvers - 1;
            avgErrorDegreesPerManeuverCount[i] = evaluationResult
                    .getAvgAbsWindCourseErrorInDegreesOfCorrectAndIncorrectWindDirectionEstimations();
            avgConfidencePerManeuverCount[i] = evaluationResult
                    .getAvgConfidenceOfCorrectAndIncorrectWindDirectionEstimations();
            avgConfidenceOfCorrectEstimationsPerManeuverCount[i] = evaluationResult
                    .getAvgConfidenceOfCorrectWindDirectionEstimations();
            avgConfidenceOfIncorrectEstimationsPerManeuverCount[i] = evaluationResult
                    .getAvgConfidenceOfIncorrectWindDirectionEstimations();
            accuracyPerManeuverCount[i] = evaluationResult.getAccuracyOfWindDirectionEstimation();
            emptyEstimationsPercentagePerManeuverCount[i] = evaluationResult
                    .getPercentageOfEmptyWindDirectionEstimations();
        }
        LoggingUtil.logInfo("Wind estimator evaluation finished with the following provided arguments:\r\n"
                + buildConfigurationString() + "\r\n");
        toCsv(avgErrorDegreesPerManeuverCount, avgConfidencePerManeuverCount,
                avgConfidenceOfCorrectEstimationsPerManeuverCount, avgConfidenceOfIncorrectEstimationsPerManeuverCount,
                accuracyPerManeuverCount, emptyEstimationsPercentagePerManeuverCount);
    }

    private static String buildConfigurationString() throws IllegalArgumentException, IllegalAccessException {
        StringBuilder str = new StringBuilder();
        for (Field field : WindEstimatorManeuverNumberDependentEvaluationRunner.class.getDeclaredFields()) {
            str.append("\t- ");
            str.append(field.getName());
            str.append(": \t ");
            str.append(field.get(null) + "\r\n");
        }
        return str.toString();
    }

    public static void toCsv(double[] avgErrorDegreesPerManeuverCount, double[] avgConfidencePerManeuverCount,
            double[] avgConfidenceOfCorrectEstimationsPerManeuverCount,
            double[] avgConfidenceOfIncorrectEstimationsPerManeuverCount, double[] accuracyPerManeuverCount,
            double[] emptyEstimationsPercentagePerManeuverCount) throws IOException {
        try (FileWriter out = new FileWriter(csvFile)) {
            String line = "Number of maneuvers; Accuracy; Percentage of empty estimations; Avg. confidence; Avg. confidence (correct races); Avg. confidence (incorrect races); Avg. error in degrees\r\n";
            System.out.println(line);
            out.write(line);
            for (int i = 0; i < MAX_MANEUVERS; i++) {
                line = (i + 1) + ";" + +accuracyPerManeuverCount[i] + ";"
                        + emptyEstimationsPercentagePerManeuverCount[i] + ";" + avgConfidencePerManeuverCount[i] + ";"
                        + avgConfidenceOfCorrectEstimationsPerManeuverCount[i] + ";"
                        + avgConfidenceOfIncorrectEstimationsPerManeuverCount[i] + ";"
                        + avgErrorDegreesPerManeuverCount[i] + "\r\n";
                System.out.println(line);
                out.write(line);
            }
        }
    }

}
