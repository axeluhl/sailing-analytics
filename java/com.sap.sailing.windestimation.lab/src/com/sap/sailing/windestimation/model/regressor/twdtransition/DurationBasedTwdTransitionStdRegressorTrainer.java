package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.model.store.PersistenceContextType;

public class DurationBasedTwdTransitionStdRegressorTrainer extends TwdTransitionAggregatedStdRegressorTrainer {

    public DurationBasedTwdTransitionStdRegressorTrainer(
            AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager,
            ModelStore regressorModelStore) {
        super(persistenceManager, regressorModelStore);
    }

    public static void main(String[] args) throws Exception {
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager durationBasedPersistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DURATION);
        MongoDbModelStore mongoDbModelStore = new MongoDbModelStore(durationBasedPersistenceManager.getDb());
        mongoDbModelStore.deleteAll(PersistenceContextType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR);
        DurationBasedTwdTransitionRegressorModelFactory durationBasedTwdTransitionRegressorModelFactory = new DurationBasedTwdTransitionRegressorModelFactory();
        for (DurationBasedTwdTransitionRegressorModelMetadata modelMetadata : durationBasedTwdTransitionRegressorModelFactory
                .getAllValidContextSpecificModelMetadataCandidates()) {
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata> model = durationBasedTwdTransitionRegressorModelFactory
                    .getNewModel(modelMetadata);
            TwdTransitionAggregatedStdRegressorTrainer trainer = new TwdTransitionAggregatedStdRegressorTrainer(
                    durationBasedPersistenceManager, mongoDbModelStore);
            trainer.trainRegressor(model);
        }
    }

}
