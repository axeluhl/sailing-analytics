package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DurationBasedTwdTransitionRegressorModelContext.DurationValueRange;
import com.sap.sailing.windestimation.model.store.FileSystemModelStoreImpl;
import com.sap.sailing.windestimation.model.store.ModelDomainType;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStoreImpl;

public class DurationBasedTwdTransitionStdRegressorTrainer extends TwdTransitionAggregatedStdRegressorTrainer {

    public DurationBasedTwdTransitionStdRegressorTrainer(
            AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager,
            ModelStore regressorModelStore) {
        super(persistenceManager, regressorModelStore);
    }

    public static void main(String[] args) throws Exception {
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager durationBasedPersistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DURATION);
        final ModelStore modelStore;
        if (args.length > 0) {
            modelStore = new FileSystemModelStoreImpl(args[0]);
        } else {
            modelStore = new MongoDbModelStoreImpl(durationBasedPersistenceManager.getDb());
        }
        train(durationBasedPersistenceManager, modelStore);
    }

    public static void train(final ModelStore modelStore) throws ModelPersistenceException, Exception {
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager durationBasedPersistenceManager =
                new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(AggregatedSingleDimensionType.DURATION);
        train(durationBasedPersistenceManager, modelStore);
    }
    
    public static void train(
            AggregatedSingleDimensionBasedTwdTransitionPersistenceManager durationBasedPersistenceManager,
            final ModelStore modelStore) throws ModelPersistenceException, Exception {
        modelStore.deleteAll(ModelDomainType.DURATION_BASED_TWD_DELTA_STD_REGRESSOR);
        DurationBasedTwdTransitionRegressorModelFactory durationBasedTwdTransitionRegressorModelFactory = new DurationBasedTwdTransitionRegressorModelFactory();
        for (DurationValueRange durationValueRange : DurationValueRange.values()) {
            DurationBasedTwdTransitionRegressorModelContext modelContext = new DurationBasedTwdTransitionRegressorModelContext(
                    durationValueRange);
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelContext> model = durationBasedTwdTransitionRegressorModelFactory
                    .getNewModel(modelContext);
            TwdTransitionAggregatedStdRegressorTrainer trainer = new TwdTransitionAggregatedStdRegressorTrainer(
                    durationBasedPersistenceManager, modelStore);
            trainer.trainRegressor(model);
        }
    }

}
