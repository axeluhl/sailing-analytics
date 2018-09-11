package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.persistence.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.data.persistence.RegularManeuversForEstimationPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.TransformedManeuversPersistenceManager;
import com.sap.sailing.windestimation.util.LoggingUtil;
import com.sap.sse.common.Util.Pair;

public class ManeuverClassifierTrainer {
    private static final double TRAIN_TEST_SPLIT_RATIO = 0.8;
    private static final int MIN_MANEUVERS_COUNT = 500;

    private final TransformedManeuversPersistenceManager<ManeuverForEstimation> persistenceManager;
    private final PolarDataService polarService;
    private List<ManeuverForEstimation> allManeuvers;
    private Map<Pair<String, ManeuverFeatures>, List<ManeuverForEstimation>> maneuversPerBoatClass = new HashMap<>();

    public ManeuverClassifierTrainer(TransformedManeuversPersistenceManager<ManeuverForEstimation> persistenceManager,
            PolarDataService polarService) {
        this.persistenceManager = persistenceManager;
        this.polarService = polarService;
    }

    public void trainClassifier(TrainableSingleManeuverOfflineClassifier classifier) throws Exception {
        Long fixesCountForBoatClass = classifier.getBoatClass() == null ? null
                : polarService.getFixCountPerBoatClass().get(classifier.getBoatClass());
        List<ManeuverForEstimation> maneuvers = getSuitableManeuvers(classifier.getManeuverFeatures(),
                classifier.getBoatClass());
        LoggingUtil.logInfo("Using " + maneuvers.size() + " maneuvers");
        if (maneuvers.size() < MIN_MANEUVERS_COUNT) {
            LoggingUtil.logInfo("Not enough maneuver data for training. Training aborted!");
        } else {
            LoggingUtil.logInfo("Splitting training and test data...");
            int splitFromIndex = (int) Math.ceil(maneuvers.size() * TRAIN_TEST_SPLIT_RATIO);
            List<ManeuverForEstimation> trainManeuvers = maneuvers.subList(0, splitFromIndex);
            LoggingUtil.logInfo("Training with  " + trainManeuvers.size() + " maneuvers...");
            classifier.trainWithManeuvers(trainManeuvers);
            LoggingUtil.logInfo("Training finished. Validating on train dataset...");
            ClassifierScoring classifierScoring = new ClassifierScoring(classifier);
            String printScoring = classifierScoring.printScoring(trainManeuvers);
            LoggingUtil.logInfo("Training score:\n" + printScoring);
            classifier.setTrainScore(classifierScoring.getLastAvgF1Score());

            LoggingUtil.logInfo("Preparing test data...");
            List<ManeuverForEstimation> testManeuvers = maneuvers.subList(splitFromIndex, maneuvers.size());
            LoggingUtil.logInfo("Validating on test dataset with " + testManeuvers.size() + " maneuvers...");
            printScoring = classifierScoring.printScoring(testManeuvers);
            LoggingUtil.logInfo("Test score:\n" + printScoring);
            classifier.setTestScore(classifierScoring.getLastAvgF1Score());
            LoggingUtil.logInfo("Persisting trained classifier...");
            classifier
                    .setFixesCountForBoatClass(fixesCountForBoatClass == null ? 0 : fixesCountForBoatClass.intValue());
            classifier.persistModel();
            LoggingUtil.logInfo("Classifier persisted successfully. Finished!");
        }
    }

    private List<ManeuverForEstimation> getSuitableManeuvers(ManeuverFeatures maneuverFeatures, BoatClass boatClass)
            throws JsonDeserializationException, ParseException {
        Pair<String, ManeuverFeatures> key = new Pair<>(boatClass == null ? null : boatClass.getName(),
                maneuverFeatures);
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
            maneuvers = allManeuvers.stream()
                    .filter(maneuver -> MLUtil.isManeuverContainsAllFeatures(maneuver, maneuverFeatures, boatClass))
                    .collect(Collectors.toList());
            maneuversPerBoatClass.put(key, maneuvers);
        }
        return maneuvers;
    }

    public static void main(String[] args) throws Exception {
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        ManeuverClassifierTrainer classifierTrainer = new ManeuverClassifierTrainer(
                new RegularManeuversForEstimationPersistenceManager(), polarService);
//        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
//            LoggingUtil.logInfo(
//                    "### Training classifier for all boat classes with maneuver features: " + maneuverFeatures);
//            List<TrainableSingleManeuverOfflineClassifier> allTrainableClassifierInstances = ManeuverClassifiersFactory
//                    .getAllTrainableClassifierInstances(maneuverFeatures, null);
//            for (TrainableSingleManeuverOfflineClassifier classifier : allTrainableClassifierInstances) {
//                LoggingUtil.logInfo("## Classifier: " + classifier.getClass().getName());
//                classifierTrainer.trainClassifier(classifier);
//            }
//        }
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();

        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            for (BoatClass boatClass : allBoatClasses) {
                LoggingUtil.logInfo("### Training classifier for boat class " + boatClass + " with maneuver features: "
                        + maneuverFeatures);
                List<TrainableSingleManeuverOfflineClassifier> allTrainableClassifierInstances = ManeuverClassifiersFactory
                        .getAllTrainableClassifierInstances(maneuverFeatures, boatClass);
                for (TrainableSingleManeuverOfflineClassifier classifier : allTrainableClassifierInstances) {
                    LoggingUtil.logInfo("## Classifier: " + classifier.getClass().getName());
                    classifierTrainer.trainClassifier(classifier);
                }
            }
        }
    }

}
