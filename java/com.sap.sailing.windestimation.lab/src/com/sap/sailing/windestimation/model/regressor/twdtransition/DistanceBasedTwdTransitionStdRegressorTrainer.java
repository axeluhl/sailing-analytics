package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.regressor.twdtransition.DistanceBasedTwdTransitionRegressorModelContext.DistanceValueRange;
import com.sap.sailing.windestimation.model.store.FileSystemModelStore;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.PersistenceContextType;

/**
 * Trains TWD delta standard deviation by considering the distance passed between two measurements.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class DistanceBasedTwdTransitionStdRegressorTrainer
        extends TwdTransitionManualTrainingDataInputRegressorTrainer {

    public DistanceBasedTwdTransitionStdRegressorTrainer(ModelStore regressorModelStore) {
        super(regressorModelStore, getTrainingDataForDistance());
    }

    public static void main(String[] args) throws Exception {
        ModelStore modelStore = new FileSystemModelStore("trained_wind_estimation_models");
        // AggregatedSingleDimensionBasedTwdTransitionPersistenceManager distanceBasedPersistenceManager = new
        // AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
        // AggregatedSingleDimensionType.DISTANCE);
        // ModelStore modelStore = new MongoDbModelStore(distanceBasedPersistenceManager.getDb());
        modelStore.deleteAll(PersistenceContextType.DISTANCE_BASED_TWD_DELTA_STD_REGRESSOR);
        DistanceBasedTwdTransitionRegressorModelFactory distanceBasedTwdTransitionRegressorModelFactory = new DistanceBasedTwdTransitionRegressorModelFactory();
        for (DistanceValueRange distanceValueRange : DistanceValueRange.values()) {
            DistanceBasedTwdTransitionRegressorModelContext modelContext = new DistanceBasedTwdTransitionRegressorModelContext(
                    distanceValueRange);
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, DistanceBasedTwdTransitionRegressorModelContext> model = distanceBasedTwdTransitionRegressorModelFactory
                    .getNewModel(modelContext);
            DistanceBasedTwdTransitionStdRegressorTrainer trainer = new DistanceBasedTwdTransitionStdRegressorTrainer(
                    modelStore);
            trainer.trainRegressor(model);
        }
    }

    public static double[][] getTrainingDataForDistance() {
        return new double[][] { { 0, 0 }, { 80, 19.3146206798595 }, { 1368, 26.2654474715468 },
                { 599103, 116.008722136383 } };
    }

}
