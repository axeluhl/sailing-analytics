package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionRegressorModelMetadata.DistanceValueRange;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.model.store.PersistenceContextType;

public class DistanceBasedTwdTransitionStdRegressorTrainer
        extends TwdTransitionManualTrainingDataInputRegressorTrainer {

    public DistanceBasedTwdTransitionStdRegressorTrainer(ModelStore regressorModelStore) {
        super(regressorModelStore, getTrainingDataForDistance());
    }

    public static void main(String[] args) throws Exception {
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager distanceBasedPersistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DISTANCE);
        MongoDbModelStore mongoDbModelStore = new MongoDbModelStore(distanceBasedPersistenceManager.getDb());
        mongoDbModelStore.deleteAll(PersistenceContextType.DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR);
        DistanceBasedTwdTransitionRegressorModelFactory distanceBasedTwdTransitionRegressorModelFactory = new DistanceBasedTwdTransitionRegressorModelFactory();
        for (DistanceValueRange distanceValueRange : DistanceValueRange.values()) {
            DistanceBasedTwdTransitionRegressorModelMetadata modelMetadata = new DistanceBasedTwdTransitionRegressorModelMetadata(
                    distanceValueRange);
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata> model = distanceBasedTwdTransitionRegressorModelFactory
                    .getNewModel(modelMetadata);
            DistanceBasedTwdTransitionStdRegressorTrainer trainer = new DistanceBasedTwdTransitionStdRegressorTrainer(
                    mongoDbModelStore);
            trainer.trainRegressor(model);
        }
    }

    public static double[][] getTrainingDataForDistance() {
        return new double[][] { { 0, 0 }, { 80, 19.3146206798595 }, { 1368, 26.2654474715468 },
                { 599103, 116.008722136383 } };
    }

}
