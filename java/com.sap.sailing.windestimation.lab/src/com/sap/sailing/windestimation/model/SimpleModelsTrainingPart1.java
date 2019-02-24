package com.sap.sailing.windestimation.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import com.sap.sailing.windestimation.data.importer.AggregatedDistanceBasedTwdTransitionImporter;
import com.sap.sailing.windestimation.data.importer.AggregatedDurationBasedTwdTransitionImporter;
import com.sap.sailing.windestimation.data.importer.DistanceBasedTwdTransitionImporter;
import com.sap.sailing.windestimation.data.importer.DurationBasedTwdTransitionImporter;
import com.sap.sailing.windestimation.data.importer.ManeuverAndWindImporter;
import com.sap.sailing.windestimation.data.importer.PolarDataImporter;
import com.sap.sailing.windestimation.data.persistence.maneuver.ManeuverForEstimationPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.RegularManeuversForEstimationPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.datavisualization.AggregatedDistanceDimensionPlot;
import com.sap.sailing.windestimation.datavisualization.AggregatedDurationDimensionPlot;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifierTrainer;
import com.sap.sailing.windestimation.model.classifier.maneuver.PersistedManeuverClassifiersScorePrinter;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionRegressorModelContext;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionRegressorModelContext.DistanceValueRange;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionRegressorModelContext;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionRegressorModelContext.DurationValueRange;
import com.sap.sailing.windestimation.util.LoggingUtil;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SimpleModelsTrainingPart1 {

    private static final int NUMBER_OF_THREADS = 3;
    private static ExecutorService executorService;

    public static void main(String[] args) throws Exception {
        new ManeuverForEstimationPersistenceManager().dropCollection();
        new RegularManeuversForEstimationPersistenceManager().dropCollection();
        executeInThreadPool(() -> PolarDataImporter.main(args));
        executeInThreadPool(() -> ManeuverAndWindImporter.main(args));
        awaitThreadPoolCompletion();
        executeInThreadPool(() -> {
            ManeuverClassifierTrainer.main(args);
            Thread.sleep(1000);
            PersistedManeuverClassifiersScorePrinter.main(args);
        });
        executeInThreadPool(() -> {
            DurationBasedTwdTransitionImporter.main(args);
        });
        executeInThreadPool(() -> {
            DistanceBasedTwdTransitionImporter.main(args);
        });
        awaitThreadPoolCompletion();
        AggregatedDurationBasedTwdTransitionImporter.createPersistenceManagerAndEnsureIndex();
        AggregatedDistanceBasedTwdTransitionImporter.createPersistenceManagerAndEnsureIndex();
        executeInThreadPool(() -> {
            AggregatedDurationBasedTwdTransitionImporter.main(args);
        });
        executeInThreadPool(() -> {
            AggregatedDistanceBasedTwdTransitionImporter.main(args);
        });
        awaitThreadPoolCompletion();
        do {
            AggregatedDurationDimensionPlot.main(args);
            showInfoAboutDataCleaning(AggregatedSingleDimensionType.DURATION);
            AggregatedDurationDimensionPlot.awaitWindowClosed();
        } while (JOptionPane.YES_OPTION != askDataCleaningFinished(AggregatedSingleDimensionType.DURATION));
        showInfoAboutIntervalAdjustments(DurationBasedTwdTransitionRegressorModelContext.class,
                DurationValueRange.class);
        do {
            AggregatedDistanceDimensionPlot.main(args);
            showInfoAboutDataCleaning(AggregatedSingleDimensionType.DISTANCE);
            AggregatedDistanceDimensionPlot.awaitWindowClosed();
        } while (JOptionPane.YES_OPTION != askDataCleaningFinished(AggregatedSingleDimensionType.DISTANCE));
        showInfoAboutIntervalAdjustments(DistanceBasedTwdTransitionRegressorModelContext.class,
                DistanceValueRange.class);
    }

    private static int askDataCleaningFinished(AggregatedSingleDimensionType dimension) {
        Object[] options = { "Continue with model training", "Restart Graphical Tool" };
        int res = JOptionPane.showOptionDialog(null,
                "Have you finished the data cleansing for the " + dimension + " dimension?",
                "Continue with model training?", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                options, options[0]);
        switch (res) {
        case 0:
            return JOptionPane.YES_OPTION;
        case 1:
            return JOptionPane.NO_OPTION;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    private static void showInfoAboutDataCleaning(AggregatedSingleDimensionType dimension) {
        JOptionPane.showMessageDialog(null, "Now, clean the data for the " + dimension
                + " dimension.\nRemove instances from MongoDB collection \"" + dimension.getCollectioName()
                + "\"\nwhich do not make sense and cause implausible zig zag sections\nwithin \"Zero mean sigma\" curve.\n\nClose the graphical tool, when you are done to resume the model training.");
    }

    private static void showInfoAboutIntervalAdjustments(Class<?> classToAdjust, Class<?> valueRangeEnum) {
        JOptionPane.showMessageDialog(null, "Now, open the source code of the class \"" + classToAdjust.getName()
                + "\".\nScroll down to the definition of the inner enum \"" + valueRangeEnum.getSimpleName()
                + "\",\nread its JavaDoc and adjust its interval definitions so that\neach interval can be learned by the adjusted regressor model configuration\nwith minimal error.\n\nPress OK after you are done.");
    }

    private static void awaitThreadPoolCompletion() throws InterruptedException {
        executorService.shutdown();
        boolean success = executorService.awaitTermination(24, TimeUnit.HOURS);
        if (!success) {
            new InterruptedException("Thread-pool was terminated after 24 hours waiting time");
        }
        Thread.sleep(1000L);
    }

    private static void executeInThreadPool(RunnableWithExceptionsCatch runnable) {
        if (executorService == null || executorService.isShutdown()) {
            createNewThreadPool();
        }
        executorService.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
                LoggingUtil.logInfo("FAILURE: Caught unexpected exception. Model training aborted");
            }
        });
    }

    private static void createNewThreadPool() {
        executorService = new ThreadPoolExecutor(NUMBER_OF_THREADS, NUMBER_OF_THREADS, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(NUMBER_OF_THREADS), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 
     * @author Vladislav Chumak (D069712)
     *
     */
    private static interface RunnableWithExceptionsCatch {
        void run() throws Exception;
    }

}
