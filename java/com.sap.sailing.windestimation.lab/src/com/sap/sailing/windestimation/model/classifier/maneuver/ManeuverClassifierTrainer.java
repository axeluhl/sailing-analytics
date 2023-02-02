package com.sap.sailing.windestimation.model.classifier.maneuver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.LabeledManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.persistence.maneuver.RegularManeuversForEstimationPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.TransformedManeuversPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.model.classifier.LabelExtraction;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.store.FileSystemModelStoreImpl;
import com.sap.sailing.windestimation.model.store.ModelDomainType;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStoreImpl;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.shared.json.JsonDeserializationException;

public class ManeuverClassifierTrainer {
    private static final Logger logger = Logger.getLogger(ManeuverClassifierTrainer.class.getName());

    private static final int MIN_MANEUVERS_COUNT = 500;

    private final TransformedManeuversPersistenceManager<LabeledManeuverForEstimation> persistenceManager;
    private List<LabeledManeuverForEstimation> allManeuvers;
    private Map<Pair<String, ManeuverFeatures>, List<LabeledManeuverForEstimation>> maneuversPerBoatClass = new HashMap<>();

    private final ModelStore classifierModelStore;

    public ManeuverClassifierTrainer(
            TransformedManeuversPersistenceManager<LabeledManeuverForEstimation> persistenceManager,
            ModelStore classifierModelStore) {
        this.persistenceManager = persistenceManager;
        this.classifierModelStore = classifierModelStore;
    }

    public void trainClassifier(
            TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> classifierModel,
            LabelExtraction<LabeledManeuverForEstimation> labelExtraction, int percentForTraining, int percentForTesting) throws Exception {
        ManeuverClassifierModelContext modelContext = classifierModel.getModelContext();
        List<LabeledManeuverForEstimation> maneuvers = getSuitableManeuvers(modelContext);
        logger.info("Using " + maneuvers.size() + " maneuvers");
        if (maneuvers.size() < MIN_MANEUVERS_COUNT) {
            logger.info("Not enough maneuver data for training. Training aborted!");
        } else {
            logger.info("Splitting training and test data...");
            Collections.sort(maneuvers, (m1, m2)->m1.getManeuverTimePoint().compareTo(m2.getManeuverTimePoint()));
            List<LabeledManeuverForEstimation> trainManeuvers = new ArrayList<>();
            List<LabeledManeuverForEstimation> testManeuvers = new ArrayList<>();
            trainManeuvers.addAll(maneuvers.subList(0, maneuvers.size()/100*percentForTraining));
            testManeuvers.addAll(maneuvers.subList(maneuvers.size()-maneuvers.size()/100*percentForTesting, maneuvers.size()));
            if (testManeuvers.size() < MIN_MANEUVERS_COUNT * 0.2 || trainManeuvers.size() < MIN_MANEUVERS_COUNT * 0.8) {
                logger.info("Not enough maneuver data for training. Training aborted!");
            } else {
                logger.info("Training with  " + trainManeuvers.size() + " maneuvers...");
                double[][] x = modelContext.getXMatrix(maneuvers);
                int[] y = labelExtraction.getYVector(maneuvers);
                classifierModel.train(x, y);
                logger.info("Training finished. Validating on train dataset...");
                ManeuverClassifierScoring classifierScoring = new ManeuverClassifierScoring(classifierModel);
                String printScoring = classifierScoring.printScoring(trainManeuvers, labelExtraction);
                logger.info("Training score:\n" + printScoring);
                double trainScore = classifierScoring.getLastAvgF1Score();
                int numberOfTrainingInstances = trainManeuvers.size();

                logger.info("Validating on test dataset with " + testManeuvers.size() + " maneuvers...");
                printScoring = classifierScoring.printScoring(testManeuvers, labelExtraction);
                logger.info("Test score:\n" + printScoring);
                double testScore = classifierScoring.getLastAvgF1Score();
                logger.info("Persisting trained classifier...");
                classifierModel.setStatsAfterSuccessfulTraining(trainScore, testScore, numberOfTrainingInstances);
                classifierModelStore.persistModel(classifierModel);
                logger.info("Classifier persisted successfully. Finished!");
            }
        }
    }

    private List<LabeledManeuverForEstimation> getSuitableManeuvers(ManeuverClassifierModelContext modelContext)
            throws JsonDeserializationException, ParseException, UnknownHostException {
        String boatClassName = modelContext.getBoatClassName();
        ManeuverFeatures maneuverFeatures = modelContext.getManeuverFeatures();
        Pair<String, ManeuverFeatures> key = new Pair<>(boatClassName, maneuverFeatures);
        List<LabeledManeuverForEstimation> maneuvers = maneuversPerBoatClass.get(key);
        if (maneuvers == null) {
            if (allManeuvers == null) {
                logger.info("Connecting to MongoDB");
                persistenceManager.createIfNotExistsCollectionWithTransformedManeuvers();
                logger.info("Querying dataset...");
                allManeuvers = persistenceManager.getAllElements();
            }
            LoggingUtil
                    .logInfo("Filtering maneuvers for boat class: " + (boatClassName == null ? "All" : boatClassName));
            maneuvers = allManeuvers.stream().filter(maneuver -> modelContext.isContainsAllFeatures(maneuver))
                    .collect(Collectors.toList());
            maneuversPerBoatClass.put(key, maneuvers);
        }
        return maneuvers;
    }

    /**
     * @param args
     *            [0] denotes percentage of maneuvers to use for training; [1] denotes percentage of maneuvers for
     *            testing; [2] is an optional path to use for a file system-based model persistence; if not provided,
     *            the models will end up in the configured MongoDB connection.
     */
    public static void main(String[] args) throws Exception {
        final RegularManeuversForEstimationPersistenceManager persistenceManager = new RegularManeuversForEstimationPersistenceManager();
        final int percentForTraining;
        final int percentForTesting;
        final ModelStore modelStore;
        if (args.length > 0) {
            percentForTraining = Integer.valueOf(args[0]);
        } else {
            percentForTraining = 80;
        }
        if (args.length > 1) {
            percentForTesting = Integer.valueOf(args[1]);
        } else {
            percentForTesting = 20;
        }
        if (args.length > 2) {
            final String modelStoragePath = args[2];
            modelStore = new FileSystemModelStoreImpl(modelStoragePath);
        } else {
            modelStore = new MongoDbModelStoreImpl(persistenceManager.getDb());
        }
        train(percentForTraining, percentForTesting, persistenceManager, modelStore);
    }
    
    public static void train(final int percentForTraining, final int percentForTesting,
            ModelStore modelStore)
            throws MalformedURLException, IOException, InterruptedException, ClassNotFoundException,
            UnknownHostException, ModelPersistenceException, Exception {
        final RegularManeuversForEstimationPersistenceManager persistenceManager = new RegularManeuversForEstimationPersistenceManager();
        train(percentForTraining, percentForTesting, persistenceManager, modelStore);
    }

    public static void train(final int percentForTraining, final int percentForTesting,
            RegularManeuversForEstimationPersistenceManager persistenceManager, ModelStore modelStore)
            throws MalformedURLException, IOException, InterruptedException, ClassNotFoundException,
            UnknownHostException, ModelPersistenceException, Exception {
        logger.info("Using "+percentForTraining+"% of maneuvers for training, "+percentForTesting+"% for testing");
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        modelStore.deleteAll(ModelDomainType.MANEUVER_CLASSIFIER);
        ManeuverClassifierTrainer classifierTrainer = new ManeuverClassifierTrainer(persistenceManager, modelStore);
        ManeuverClassifierModelFactory classifierModelFactory = new ManeuverClassifierModelFactory();
        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            logger.info(
                    "### Training classifier for all boat classes with maneuver features: " + maneuverFeatures);
            ManeuverClassifierModelContext modelContext = new ManeuverClassifierModelContext(maneuverFeatures, null,
                    ManeuverClassifierModelFactory.orderedSupportedTargetValues);
            ManeuverLabelExtraction labelExtraction = new ManeuverLabelExtraction(modelContext);
            TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> classifierModel = classifierModelFactory
                    .getNewModel(modelContext);
            logger.info("## Classifier: " + classifierModel.getClass().getName());
            classifierTrainer.trainClassifier(classifierModel, labelExtraction, percentForTraining, percentForTesting);
        }
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();
        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            for (BoatClass boatClass : allBoatClasses) {
                logger.info("### Training classifier for boat class " + boatClass + " with maneuver features: " + maneuverFeatures);
                ManeuverClassifierModelContext modelContext = new ManeuverClassifierModelContext(maneuverFeatures,
                        boatClass.getName(), ManeuverClassifierModelFactory.orderedSupportedTargetValues);
                ManeuverLabelExtraction labelExtraction = new ManeuverLabelExtraction(modelContext);
                TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> classifierModel = classifierModelFactory
                        .getNewModel(modelContext);
                logger.info("## Classifier: " + classifierModel.getClass().getName());
                classifierTrainer.trainClassifier(classifierModel, labelExtraction, percentForTraining, percentForTesting);
            }
        }
    }
}
