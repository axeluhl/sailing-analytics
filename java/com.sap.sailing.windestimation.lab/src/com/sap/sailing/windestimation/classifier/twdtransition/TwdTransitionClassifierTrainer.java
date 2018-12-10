package com.sap.sailing.windestimation.classifier.twdtransition;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.classifier.store.ClassifierModelStore;
import com.sap.sailing.windestimation.classifier.store.MongoDbClassifierModelStore;
import com.sap.sailing.windestimation.data.LabelledTwdTransition;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.importer.TwdTransitionImporter;
import com.sap.sailing.windestimation.data.persistence.polars.PolarDataServiceAccessUtil;
import com.sap.sailing.windestimation.data.persistence.wind.TwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class TwdTransitionClassifierTrainer {
    // private static final double TRAIN_TEST_SPLIT_RATIO = 0.8;
    private static final int MIN_TWD_TRANSITIONS_COUNT = 500 * 4 * 4;

    private final TwdTransitionPersistenceManager persistenceManager;
    private List<TwdTransition> allTwdTransitions;

    private final ClassifierModelStore classifierModelStore;

    public TwdTransitionClassifierTrainer(TwdTransitionPersistenceManager persistenceManager,
            ClassifierModelStore classifierModelStore) {
        this.persistenceManager = persistenceManager;
        this.classifierModelStore = classifierModelStore;
    }

    public void trainClassifier(TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata> classifierModel)
            throws Exception {
        TwdTransitionModelMetadata modelMetadata = classifierModel.getModelMetadata().getContextSpecificModelMetadata();
        List<TwdTransition> twdTransitions = getSuitableManeuvers(modelMetadata);
        LoggingUtil.logInfo("Using " + twdTransitions.size() + " twd transitions instances");
        if (twdTransitions.size() < MIN_TWD_TRANSITIONS_COUNT) {
            LoggingUtil.logInfo("Not enough instances for training. Training aborted!");
        } else {
            LoggingUtil.logInfo("Splitting training and test data...");
            List<TwdTransition> trainInstances = new ArrayList<>();
            List<TwdTransition> testInstances = new ArrayList<>();
            for (TwdTransition twdTransition : twdTransitions) {
                if (((LabelledTwdTransition) twdTransition).getRegattaName().contains("2018")) {
                    testInstances.add(twdTransition);
                } else {
                    trainInstances.add(twdTransition);
                }
            }
            if (testInstances.size() < MIN_TWD_TRANSITIONS_COUNT * 0.2
                    || trainInstances.size() < MIN_TWD_TRANSITIONS_COUNT * 0.8) {
                LoggingUtil.logInfo("Not enough instances for training. Training aborted!");
            } else {
                LoggingUtil.logInfo("Training with  " + trainInstances.size() + " instances...");
                double[][] x = modelMetadata.getXMatrix(twdTransitions);
                int[] y = modelMetadata.getYVector(twdTransitions);
                classifierModel.train(x, y);
                LoggingUtil.logInfo("Training finished. Validating on train dataset...");
                TwdTransitionClassifierScoring classifierScoring = new TwdTransitionClassifierScoring(classifierModel);
                String printScoring = classifierScoring.printScoring(trainInstances);
                LoggingUtil.logInfo("Training score:\n" + printScoring);
                double trainScore = classifierScoring.getLastAvgF1Score();
                int numberOfTrainingInstances = trainInstances.size();

                LoggingUtil.logInfo("Validating on test dataset with " + testInstances.size() + " maneuvers...");
                printScoring = classifierScoring.printScoring(testInstances);
                LoggingUtil.logInfo("Test score:\n" + printScoring);
                double testScore = classifierScoring.getLastAvgF1Score();
                LoggingUtil.logInfo("Persisting trained classifier...");
                classifierModel.setTrainingStats(trainScore, testScore, numberOfTrainingInstances);
                classifierModelStore.persistState(classifierModel);
                LoggingUtil.logInfo("Classifier persisted successfully. Finished!");
            }
        }
    }

    private List<TwdTransition> getSuitableManeuvers(TwdTransitionModelMetadata modelMetadata)
            throws JsonDeserializationException, ParseException, UnknownHostException {
        BoatClass boatClass = modelMetadata.getBoatClass();
        if (allTwdTransitions == null) {
            LoggingUtil.logInfo("Connecting to MongoDB");
            if (!persistenceManager.collectionExists()) {
                TwdTransitionImporter.main(new String[0]);
            }
            LoggingUtil.logInfo("Querying dataset...");
            allTwdTransitions = persistenceManager.getAllElements();
        }
        List<TwdTransition> result = allTwdTransitions;
        if (boatClass != null) {
            LoggingUtil.logInfo("Filtering instances for boat class: " + boatClass.getName());
            result = allTwdTransitions.stream().filter(instance -> modelMetadata.isContainsAllFeatures(instance))
                    .collect(Collectors.toList());
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        PolarDataService polarService = PolarDataServiceAccessUtil.getPersistedPolarService();
        TwdTransitionPersistenceManager persistenceManager = new TwdTransitionPersistenceManager();
        ClassifierModelStore classifierModelStore = new MongoDbClassifierModelStore(persistenceManager.getDb());
        TwdTransitionClassifierTrainer classifierTrainer = new TwdTransitionClassifierTrainer(persistenceManager,
                classifierModelStore);
        TwdTransitionClassifierModelFactory classifierModelFactory = new TwdTransitionClassifierModelFactory();
        LoggingUtil.logInfo("### Training classifier for all boat classes");
        TwdTransitionModelMetadata modelMetadata = new TwdTransitionModelMetadata(null);
        List<TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata>> allTrainableModels = classifierModelFactory
                .getAllTrainableClassifierModels(modelMetadata);
        for (TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata> classifierModel : allTrainableModels) {
            LoggingUtil.logInfo("## Classifier: " + classifierModel.getClass().getName());
            classifierTrainer.trainClassifier(classifierModel);
        }
        Set<BoatClass> allBoatClasses = polarService.getAllBoatClassesWithPolarSheetsAvailable();
        for (BoatClass boatClass : allBoatClasses) {
            LoggingUtil.logInfo("### Training classifier for boat class " + boatClass);
            TwdTransitionModelMetadata twdTransitionModelMetadata = new TwdTransitionModelMetadata(boatClass);
            List<TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata>> allTrainableClassifierModels = classifierModelFactory
                    .getAllTrainableClassifierModels(twdTransitionModelMetadata);
            for (TrainableClassificationModel<TwdTransition, TwdTransitionModelMetadata> classifierModel : allTrainableClassifierModels) {
                LoggingUtil.logInfo("## Classifier: " + classifierModel.getClass().getName());
                classifierTrainer.trainClassifier(classifierModel);
            }
        }
    }

}
