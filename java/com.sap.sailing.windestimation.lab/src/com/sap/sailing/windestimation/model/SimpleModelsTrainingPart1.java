package com.sap.sailing.windestimation.model;

import java.util.concurrent.ExecutorService;
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
import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionStdRegressorTrainer;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionRegressorModelContext.DistanceValueRange;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionRegressorModelContext;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionStdRegressorTrainer;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionRegressorModelContext.DurationValueRange;
import com.sap.sailing.windestimation.model.store.FileSystemModelStoreImpl;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStoreImpl;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.util.ThreadPoolUtil;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SimpleModelsTrainingPart1 {
    private static final int NUMBER_OF_THREADS = 15;
    private static ExecutorService executorService;

    /**
     * @param args
     *            {@code args[0]} must contain a valid bearer token that authenticates a user with {@code EXPORT}
     *            permission on the {@code TRACKED_RACE}s for wind data access. Only regattas/races are considered that
     *            the user authenticated by this token can {@code READ}. {@code args[1]} may contain a percentage of the
     *            maneuvers to use for training which defaults to 80; {@code args[2]} may contain a percentage of the
     *            maneuvers to use for testing which defaults to 20. If {@code args[3]} is also provided, it is taken to
     *            be the file system path for storing the models that result from the training process; with this, the
     *            models are not stored in MongoDB which otherwise would be the default.
     */
    public static void main(String[] args) throws Exception {
        final String bearerToken = args[0];
        final int percentForTraining;
        final int percentForTesting;
        if (args.length > 1) {
            percentForTraining = Integer.valueOf(args[1]);
        } else {
            percentForTraining = 80;
        }
        if (args.length > 2) {
            percentForTesting = Integer.valueOf(args[2]);
        } else {
            percentForTesting = 20;
        }
        final ManeuverForEstimationPersistenceManager maneuverForEstimationPersistenceManager = new ManeuverForEstimationPersistenceManager();
        final ModelStore modelStore;
        if (args.length > 3) {
            modelStore = new FileSystemModelStoreImpl(args[3]);
        } else {
            modelStore = new MongoDbModelStoreImpl(maneuverForEstimationPersistenceManager.getDb());
        }
        maneuverForEstimationPersistenceManager.dropCollection();
        new RegularManeuversForEstimationPersistenceManager().dropCollection();
        executeInThreadPool(() -> PolarDataImporter.main(args));
        executeInThreadPool(() -> ManeuverAndWindImporter.main(new String[] { bearerToken }));
        awaitThreadPoolCompletion();
        executeInThreadPool(() -> {
            ManeuverClassifierTrainer.train(percentForTraining, percentForTesting, modelStore);
            Thread.sleep(1000);
            PersistedManeuverClassifiersScorePrinter.main(new String[0]);
        });
        executeInThreadPool(() -> {
            DistanceBasedTwdTransitionImporter.main(new String[0]);
        });
        executeInThreadPool(() -> {
            DurationBasedTwdTransitionImporter.main(new String[0]);
        });
        awaitThreadPoolCompletion();
        AggregatedDurationBasedTwdTransitionImporter.createPersistenceManagerAndEnsureIndex();
        AggregatedDistanceBasedTwdTransitionImporter.createPersistenceManagerAndEnsureIndex();
        executeInThreadPool(() -> {
            AggregatedDurationBasedTwdTransitionImporter.main(new String[0]);
        });
        executeInThreadPool(() -> {
            AggregatedDistanceBasedTwdTransitionImporter.main(new String[0]);
        });
        awaitThreadPoolCompletion();
        // FIXME bug5695: enforce monotonic "Zero Mean Sigma", maybe considering number of values that formed the aggregate, then run SimpleModelsTrainingPart2
        // Idea: rather ignore a value with low number of samples and use values with higher number of samples (getNumberOfValues) if there are different ways to achieve monotonicity
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
        showInfoAboutRunPart2();
        // FIXME bug5695: now comes "part 2"
        DurationBasedTwdTransitionStdRegressorTrainer.train(modelStore);
        DistanceBasedTwdTransitionStdRegressorTrainer.train(modelStore);
        Thread.sleep(1000);
        ExportedModelsGenerator.main(new String[0]);
        LoggingUtil.logInfo("Model training finished. You can upload the generated file to a server instance.");
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

    private static void showInfoAboutRunPart2() {
        JOptionPane.showMessageDialog(null, "Now, run the class \"" + SimpleModelsTrainingPart2.class.getName()
                + "\".\nThis will complete the training process.");
    }

    private static void showInfoAboutDataCleaning(AggregatedSingleDimensionType dimension) {
        JOptionPane.showMessageDialog(null, "Now, clean the data for the " + dimension
                + " dimension.\nRemove instances from MongoDB collection \"" + dimension.getCollectionName()
                + "\"\nwhich do not make sense and cause implausible zig zag sections\nwithin \"Zero mean sigma\" curve.\n\nClose the graphical tool, when you are done to resume the model training.");
    }

    private static void showInfoAboutIntervalAdjustments(Class<?> classToAdjust, Class<?> valueRangeEnum) {
        JOptionPane.showMessageDialog(null, "Now, open the source code of the class \"" + classToAdjust.getName()
                + "\".\nScroll down to the definition of the inner enum \"" + valueRangeEnum.getSimpleName()
                + "\",\nread its JavaDoc and adjust its interval definitions so that\neach interval can be learned by the adjusted regressor model configuration\nwith minimal error.\n\nPress OK after you are done.");
    }

    private static void awaitThreadPoolCompletion() throws InterruptedException {
        executorService.shutdown();
        final long TIMEOUT_IN_HOURS = 48;
        boolean success = executorService.awaitTermination(TIMEOUT_IN_HOURS, TimeUnit.HOURS);
        if (!success) {
            LoggingUtil.logInfo("Thread-pool was terminated after "+TIMEOUT_IN_HOURS+
                    " hours waiting time. Launching next step. You may, e.g., be seeing an empty chart in case the process is really still running."+
                    " In this case, please follow the log and keep refreshing until you see content.");
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
        executorService = ThreadPoolUtil.INSTANCE.createForegroundTaskThreadPoolExecutor(NUMBER_OF_THREADS,
                SimpleModelsTrainingPart1.class.getName());
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
