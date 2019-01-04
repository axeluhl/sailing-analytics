package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.TwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.model.classifier.LabelExtraction;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.store.ContextType;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class TwdTransitionClassifierTrainer {
    private final TwdTransitionPersistenceManager persistenceManager;
    private final ModelStore classifierModelStore;

    public TwdTransitionClassifierTrainer(TwdTransitionPersistenceManager persistenceManager,
            ModelStore classifierModelStore) {
        this.persistenceManager = persistenceManager;
        this.classifierModelStore = classifierModelStore;
    }

    public void trainClassifier(
            TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> classifierModel,
            LabelExtraction<TwdTransition> labelExtraction) throws Exception {
        TwdTransitionClassifierModelMetadata modelMetadata = classifierModel.getContextSpecificModelMetadata();
        LoggingUtil.logInfo("Querying dataset...");
        int numberOfTrainingInstances = (int) persistenceManager.countElements();
        LoggingUtil.logInfo("Using " + numberOfTrainingInstances + " twd transitions instances");
        LoggingUtil.logInfo("Converting dataset to array...");
        double[][] x = new double[numberOfTrainingInstances][1];
        int[] y = new int[numberOfTrainingInstances];
        PersistedElementsIterator<TwdTransition> iterator = persistenceManager.getIterator();
        int i = 0;
        while (iterator.hasNext()) {
            TwdTransition twdTransition = iterator.next();
            x[i][0] = modelMetadata.getX(twdTransition)[0];
            y[i] = labelExtraction.getY(twdTransition);
            i++;
        }
        LoggingUtil.logInfo("Training with  " + numberOfTrainingInstances + " instances...");
        classifierModel.train(x, y);
        LoggingUtil.logInfo("Training finished. Validating on train dataset...");
        TwdTransitionClassifierScoring classifierScoring = new TwdTransitionClassifierScoring(classifierModel);
        String printScoring = classifierScoring.printScoring(x, y);
        LoggingUtil.logInfo("Training score:\n" + printScoring);
        double trainScore = classifierScoring.getLastAvgF1Score();
        LoggingUtil.logInfo("Persisting trained classifier...");
        classifierModel.setTrainingStats(trainScore, trainScore, numberOfTrainingInstances);
        classifierModelStore.persistState(classifierModel);
        LoggingUtil.logInfo("Classifier persisted successfully. Finished!");
    }

    public static void main(String[] args) throws Exception {
        TwdTransitionPersistenceManager persistenceManager = new TwdTransitionPersistenceManager();
        ModelStore classifierModelStore = new MongoDbModelStore(persistenceManager.getDb());
        classifierModelStore.deleteAll(ContextType.TWD_TRANSITION);
        TwdTransitionClassifierTrainer classifierTrainer = new TwdTransitionClassifierTrainer(persistenceManager,
                classifierModelStore);
        TwdTransitionClassifierModelFactory classifierModelFactory = new TwdTransitionClassifierModelFactory();
        for (TwdTransitionClassifierModelMetadata modelMetadata : classifierModelFactory
                .getAllValidContextSpecificModelMetadataCandidates(null)) {
            for (TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelMetadata> model : classifierModelFactory
                    .getAllTrainableModels(modelMetadata)) {
                LoggingUtil.logInfo("## Classifier: " + model.getClass().getName());
                classifierTrainer.trainClassifier(model,
                        new LabelledTwdTransitionClassifierModelMetadata(modelMetadata.getManeuverTypeTransition()));
            }
        }
    }

}
