package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverClassifierModelFactory;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverFeatures;
import com.sap.sailing.windestimation.classifier.maneuver.ManeuverModelMetadata;
import com.sap.sailing.windestimation.classifier.store.ClassifierModelStore;
import com.sap.sailing.windestimation.classifier.store.MongoDbClassifierModelStore;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.persistence.maneuver.RegularManeuversForEstimationPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.TransformedManeuversPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.Util.Pair;

public class ManeuverClassifierTrainer {
    // private static final double TRAIN_TEST_SPLIT_RATIO = 0.8;
    private static final int MIN_MANEUVERS_COUNT = 500;

    private final TransformedManeuversPersistenceManager<ManeuverForEstimation> persistenceManager;
    private List<ManeuverForEstimation> allManeuvers;
    private Map<Pair<BoatClass, ManeuverFeatures>, List<ManeuverForEstimation>> maneuversPerBoatClass = new HashMap<>();

    private final ClassifierModelStore classifierModelStore;

    public ManeuverClassifierTrainer(TransformedManeuversPersistenceManager<ManeuverForEstimation> persistenceManager,
            ClassifierModelStore classifierModelStore) {
        this.persistenceManager = persistenceManager;
        this.classifierModelStore = classifierModelStore;
    }

    public void trainClassifier(
            TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> classifierModel)
            throws Exception {
        ManeuverModelMetadata modelMetadata = classifierModel.getModelMetadata().getContextSpecificModelMetadata();
        List<ManeuverForEstimation> maneuvers = getSuitableManeuvers(modelMetadata);
        LoggingUtil.logInfo("Using " + maneuvers.size() + " maneuvers");
        if (maneuvers.size() < MIN_MANEUVERS_COUNT) {
            LoggingUtil.logInfo("Not enough maneuver data for training. Training aborted!");
        } else {
            LoggingUtil.logInfo("Splitting training and test data...");
            List<ManeuverForEstimation> trainManeuvers = new ArrayList<>();
            List<ManeuverForEstimation> testManeuvers = new ArrayList<>();
            for (ManeuverForEstimation maneuver : maneuvers) {
                if (maneuver.getRegattaName().contains("2018")) {
                    testManeuvers.add(maneuver);
                } else {
                    trainManeuvers.add(maneuver);
                }
            }
            if (testManeuvers.size() < MIN_MANEUVERS_COUNT * 0.2 || trainManeuvers.size() < MIN_MANEUVERS_COUNT * 0.8) {
                LoggingUtil.logInfo("Not enough maneuver data for training. Training aborted!");
            } else {
                LoggingUtil.logInfo("Training with  " + trainManeuvers.size() + " maneuvers...");
                double[][] x = modelMetadata.getXMatrix(maneuvers);
                int[] y = modelMetadata.getYVector(maneuvers);
                classifierModel.train(x, y);
                LoggingUtil.logInfo("Training finished. Validating on train dataset...");
                ClassifierScoring classifierScoring = new ClassifierScoring(classifierModel);
                String printScoring = classifierScoring.printScoring(trainManeuvers);
                LoggingUtil.logInfo("Training score:\n" + printScoring);
                double trainScore = classifierScoring.getLastAvgF1Score();
                int numberOfTrainingInstances = trainManeuvers.size();

                LoggingUtil.logInfo("Validating on test dataset with " + testManeuvers.size() + " maneuvers...");
                printScoring = classifierScoring.printScoring(testManeuvers);
                LoggingUtil.logInfo("Test score:\n" + printScoring);
                double testScore = classifierScoring.getLastAvgF1Score();
                LoggingUtil.logInfo("Persisting trained classifier...");
                classifierModel.setTrainingStats(trainScore, testScore, numberOfTrainingInstances);
                classifierModelStore.persistState(classifierModel);
                LoggingUtil.logInfo("Classifier persisted successfully. Finished!");
            }
        }
    }

    private List<ManeuverForEstimation> getSuitableManeuvers(ManeuverModelMetadata modelMetadata)
            throws JsonDeserializationException, ParseException {
        BoatClass boatClass = modelMetadata.getBoatClass();
        ManeuverFeatures maneuverFeatures = modelMetadata.getManeuverFeatures();
        Pair<BoatClass, ManeuverFeatures> key = new Pair<>(boatClass, maneuverFeatures);
        List<ManeuverForEstimation> maneuvers = maneuversPerBoatClass.get(key);
        if (maneuvers == null) {
            if (allManeuvers == null) {
                LoggingUtil.logInfo("Connecting to MongoDB");
                persistenceManager.createIfNotExistsCollectionWithTransformedManeuvers();
                LoggingUtil.logInfo("Querying dataset...");
                allManeuvers = persistenceManager.getAllElements();
            }
            LoggingUtil.logInfo(
                    "Filtering maneuvers for boat class: " + (boatClass == null ? "All" : boatClass.getName()));
            maneuvers = allManeuvers.stream().filter(maneuver -> modelMetadata.isContainsAllFeatures(maneuver))
                    .collect(Collectors.toList());
            maneuversPerBoatClass.put(key, maneuvers);
        }
        return maneuvers;
    }

    public static void main(String[] args) throws Exception {
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        RegularManeuversForEstimationPersistenceManager persistenceManager = new RegularManeuversForEstimationPersistenceManager();
        ClassifierModelStore classifierModelStore = new MongoDbClassifierModelStore(persistenceManager.getDb());
        classifierModelStore.deleteAll();
        ManeuverClassifierTrainer classifierTrainer = new ManeuverClassifierTrainer(persistenceManager,
                classifierModelStore);
        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            LoggingUtil.logInfo(
                    "### Training classifier for all boat classes with maneuver features: " + maneuverFeatures);
            List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> allTrainableModels = ManeuverClassifierModelFactory
                    .getAllTrainableClassifierModels(maneuverFeatures, null);
            for (TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> classifierModel : allTrainableModels) {
                LoggingUtil.logInfo("## Classifier: " + classifierModel.getClass().getName());
                classifierTrainer.trainClassifier(classifierModel);
            }
        }
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();

        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            for (BoatClass boatClass : allBoatClasses) {
                LoggingUtil.logInfo("### Training classifier for boat class " + boatClass + " with maneuver features: "
                        + maneuverFeatures);
                List<TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata>> allTrainableClassifierModels = ManeuverClassifierModelFactory
                        .getAllTrainableClassifierModels(maneuverFeatures, boatClass);
                for (TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> classifierModel : allTrainableClassifierModels) {
                    LoggingUtil.logInfo("## Classifier: " + classifierModel.getClass().getName());
                    classifierTrainer.trainClassifier(classifierModel);
                }
            }
        }
    }

}
