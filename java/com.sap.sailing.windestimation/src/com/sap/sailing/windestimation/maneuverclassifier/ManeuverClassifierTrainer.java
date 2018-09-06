package com.sap.sailing.windestimation.maneuverclassifier;

import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.persistence.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.data.persistence.RegularManeuversForEstimationPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.TransformedManeuversPersistenceManager;
import com.sap.sailing.windestimation.maneuverclassifier.impl.ManeuverFeatures;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class ManeuverClassifierTrainer {
    private static final double TRAIN_TEST_SPLIT_RATIO = 0.8;
    private static final int MIN_MANEUVERS_COUNT = 500;

    private final TransformedManeuversPersistenceManager<ManeuverForEstimation> persistenceManager;
    private final PolarDataService polarService;

    public ManeuverClassifierTrainer(TransformedManeuversPersistenceManager<ManeuverForEstimation> persistenceManager,
            PolarDataService polarService) {
        this.persistenceManager = persistenceManager;
        this.polarService = polarService;
    }

    public void trainClassifier(TrainableSingleManeuverOfflineClassifier classifier) throws Exception {
        LoggingUtil.logInfo("Connecting to MongoDB");
        persistenceManager.createIfNotExistsCollectionWithTransformedManeuvers();
        LoggingUtil.logInfo("Querying train dataset...");
        BoatClass boatClass = classifier.getBoatClass();
        Long fixesCountForBoatClass = polarService.getFixCountPerBoatClass().get(boatClass);
        String query = null;
        if (boatClass != null) {
            query = "{\"boatClass\" : {$eq: \"" + boatClass.getName() + "\"}}";
        }
        List<ManeuverForEstimation> maneuvers = persistenceManager.getAllElements(query);
        LoggingUtil.logInfo("Queried " + maneuvers.size() + " maneuvers");
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
            maneuvers = persistenceManager.getAllElements(query);
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

    public static void main(String[] args) throws Exception {
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();
        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            LoggingUtil.logInfo("##### Training classifier with features: " + maneuverFeatures);
            TrainableSingleManeuverOfflineClassifier classifier = ManeuverClassifiersFactory
                    .getNewClassifierInstance(maneuverFeatures, null);
            ManeuverClassifierTrainer classifierTrainer = new ManeuverClassifierTrainer(
                    new RegularManeuversForEstimationPersistenceManager(), polarService);
            classifierTrainer.trainClassifier(classifier);
        }

        for (ManeuverFeatures maneuverFeatures : ManeuverFeatures.values()) {
            for (BoatClass boatClass : allBoatClasses) {
                LoggingUtil.logInfo("### Training classifier for boat class " + boatClass + " with maneuver features: "
                        + maneuverFeatures);
                TrainableSingleManeuverOfflineClassifier classifier = ManeuverClassifiersFactory
                        .getNewClassifierInstance(maneuverFeatures, boatClass);
                ManeuverClassifierTrainer classifierTrainer = new ManeuverClassifierTrainer(
                        new RegularManeuversForEstimationPersistenceManager(), polarService);
                classifierTrainer.trainClassifier(classifier);
            }
        }
    }

}
