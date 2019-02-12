package com.sap.sailing.windestimation.model.classifier.twdtransition;

import com.sap.sailing.windestimation.data.LabelledTwdTransition;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.TwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.serialization.TwdTransitionJsonSerializer;
import com.sap.sailing.windestimation.model.classifier.LabelExtraction;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.store.ModelDomainType;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStoreImpl;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class TwdTransitionClassifierTrainer {
    private final TwdTransitionPersistenceManager persistenceManager;
    private final ModelStore classifierModelStore;

    public TwdTransitionClassifierTrainer(TwdTransitionPersistenceManager persistenceManager,
            ModelStore classifierModelStore) {
        this.persistenceManager = persistenceManager;
        this.classifierModelStore = classifierModelStore;
    }

    private static String getAndClauseForQuery(ManeuverTypeForClassification fromManeuverType,
            ManeuverTypeForClassification toManeuverType) {
        return "{$and: [{\"" + TwdTransitionJsonSerializer.FROM_MANEUVER_TYPE + "\": " + fromManeuverType.ordinal()
                + "}, {\"" + TwdTransitionJsonSerializer.TO_MANEUVER_TYPE + "\": " + toManeuverType.ordinal() + "}]}";
    }

    private static String getOrClauseForQuery(String... andClauses) {
        StringBuilder str = new StringBuilder("{$or: [");
        for (int i = 0; i < andClauses.length; i++) {
            String andClause = andClauses[i];
            if (i > 0) {
                str.append(", ");
            }
            str.append(andClause);
        }
        str.append("]}");
        return str.toString();
    }

    public void trainClassifier(
            TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext> classifierModel,
            LabelExtraction<LabelledTwdTransition> labelExtraction) throws Exception {
        TwdTransitionClassifierModelContext modelContext = classifierModel.getModelContext();
        LoggingUtil.logInfo("Querying dataset...");
        String query = "Invalid query";
        switch (modelContext.getManeuverTypeTransition()) {
        case TACK_TACK:
            query = getAndClauseForQuery(ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.TACK);
            break;
        case TACK_JIBE:
            query = getOrClauseForQuery(
                    getAndClauseForQuery(ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.JIBE),
                    getAndClauseForQuery(ManeuverTypeForClassification.JIBE, ManeuverTypeForClassification.TACK));
            break;
        case TACK_OTHER:
            query = getOrClauseForQuery(
                    getAndClauseForQuery(ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.BEAR_AWAY),
                    getAndClauseForQuery(ManeuverTypeForClassification.TACK, ManeuverTypeForClassification.HEAD_UP),
                    getAndClauseForQuery(ManeuverTypeForClassification.BEAR_AWAY, ManeuverTypeForClassification.TACK),
                    getAndClauseForQuery(ManeuverTypeForClassification.HEAD_UP, ManeuverTypeForClassification.TACK));
            break;
        case JIBE_JIBE:
            query = getAndClauseForQuery(ManeuverTypeForClassification.JIBE, ManeuverTypeForClassification.JIBE);
            break;
        case JIBE_OTHER:
            query = getOrClauseForQuery(
                    getAndClauseForQuery(ManeuverTypeForClassification.JIBE, ManeuverTypeForClassification.BEAR_AWAY),
                    getAndClauseForQuery(ManeuverTypeForClassification.JIBE, ManeuverTypeForClassification.HEAD_UP),
                    getAndClauseForQuery(ManeuverTypeForClassification.BEAR_AWAY, ManeuverTypeForClassification.JIBE),
                    getAndClauseForQuery(ManeuverTypeForClassification.HEAD_UP, ManeuverTypeForClassification.JIBE));
            break;
        case OTHER_OTHER:
            query = getOrClauseForQuery(
                    getAndClauseForQuery(ManeuverTypeForClassification.BEAR_AWAY,
                            ManeuverTypeForClassification.BEAR_AWAY),
                    getAndClauseForQuery(ManeuverTypeForClassification.BEAR_AWAY,
                            ManeuverTypeForClassification.HEAD_UP),
                    getAndClauseForQuery(ManeuverTypeForClassification.HEAD_UP,
                            ManeuverTypeForClassification.BEAR_AWAY),
                    getAndClauseForQuery(ManeuverTypeForClassification.HEAD_UP, ManeuverTypeForClassification.HEAD_UP));
            break;
        }
        PersistedElementsIterator<LabelledTwdTransition> iterator = persistenceManager.getIterator(query);
        int numberOfTrainingInstances = (int) iterator.getNumberOfElements();
        LoggingUtil.logInfo("Using " + numberOfTrainingInstances + " twd transitions instances");
        LoggingUtil.logInfo("Allocating array...");
        double[][] x = new double[numberOfTrainingInstances][1];
        int[] y = new int[numberOfTrainingInstances];
        int i = 0;
        LoggingUtil.logInfo("Converting dataset to array...");
        while (iterator.hasNext()) {
            LabelledTwdTransition twdTransition = iterator.next();
            x[i][0] = modelContext.getXAsSingleValue(twdTransition);
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
        classifierModelStore.persistModel(classifierModel);
        LoggingUtil.logInfo("Classifier persisted successfully. Finished!");
    }

    public static void main(String[] args) throws Exception {
        TwdTransitionPersistenceManager persistenceManager = new TwdTransitionPersistenceManager();
        ModelStore classifierModelStore = new MongoDbModelStoreImpl(persistenceManager.getDb());
        classifierModelStore.deleteAll(ModelDomainType.TWD_TRANSITION_CLASSIFIER);
        TwdTransitionClassifierTrainer classifierTrainer = new TwdTransitionClassifierTrainer(persistenceManager,
                classifierModelStore);
        TwdTransitionLabelExtraction labelExtraction = new TwdTransitionLabelExtraction();
        TwdTransitionClassifierModelFactory classifierModelFactory = new TwdTransitionClassifierModelFactory();
        for (ManeuverTypeTransition maneuverTypeTransition : ManeuverTypeTransition.values()) {
            TwdTransitionClassifierModelContext modelContext = new TwdTransitionClassifierModelContext(
                    maneuverTypeTransition);
            LoggingUtil.logInfo("## ManeuverTypeTransition: " + maneuverTypeTransition);
            for (TrainableClassificationModel<TwdTransition, TwdTransitionClassifierModelContext> model : classifierModelFactory
                    .getAllTrainableModels(modelContext)) {
                LoggingUtil.logInfo("## Classifier: " + model.getClass().getName());
                classifierTrainer.trainClassifier(model, labelExtraction);
            }
        }
    }

}
