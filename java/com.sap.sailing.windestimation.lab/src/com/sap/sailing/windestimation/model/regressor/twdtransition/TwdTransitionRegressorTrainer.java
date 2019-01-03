package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class TwdTransitionRegressorTrainer {

    private final AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager;
    private final ModelStore regressorModelStore;

    public TwdTransitionRegressorTrainer(
            AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager,
            ModelStore regressorModelStore) {
        this.persistenceManager = persistenceManager;
        this.regressorModelStore = regressorModelStore;
    }

    public void trainRegressor(
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, ? extends SingleDimensionBasedTwdTransitionRegressorModelMetadata> model)
            throws Exception {
        PersistedElementsIterator<AggregatedSingleDimensionBasedTwdTransition> iterator = persistenceManager
                .getIterator();
        double[] x = new double[1];
        LoggingUtil
                .logInfo("########## Training of " + model.getContextSpecificModelMetadata().getId() + " started...");
        long numberOfTrainingInstances = 0;
        while (iterator.hasNext()) {
            AggregatedSingleDimensionBasedTwdTransition twdTransition = iterator.next();
            double dimensionValue = twdTransition.getDimensionValue();
            if (model.getContextSpecificModelMetadata().isDimensionValueSupported(dimensionValue)) {
                x[0] = dimensionValue;
                model.train(x, twdTransition.getStd());
                numberOfTrainingInstances += twdTransition.getNumberOfValues();
            }
        }
        persistenceManager.getIterator();
        LoggingUtil.logInfo("Calculating root mean square error ...");
        double squareErrorSum = 0;
        int numberOfAggregates = 0;
        while (iterator.hasNext()) {
            AggregatedSingleDimensionBasedTwdTransition twdTransition = iterator.next();
            double dimensionValue = twdTransition.getDimensionValue();
            if (model.getContextSpecificModelMetadata().isDimensionValueSupported(dimensionValue)) {
                x[0] = dimensionValue;
                double predictedStd = model.getValue(x);
                double diff = predictedStd - twdTransition.getStd();
                squareErrorSum += diff * diff;
                numberOfAggregates++;
            }
        }
        double rootMeanSquareError = Math.sqrt(squareErrorSum / numberOfAggregates);
        LoggingUtil.logInfo("Root mean square error = " + rootMeanSquareError);
        model.setTrainingStats(rootMeanSquareError, rootMeanSquareError, numberOfTrainingInstances);
        LoggingUtil.logInfo("Persisting trained regressor...");
        regressorModelStore.persistState(model);
        LoggingUtil.logInfo("Classifier persisted successfully. Finished!");
    }

    public static void main(String[] args) throws Exception {
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager distanceBasedPersistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DISTANCE);
        MongoDbModelStore mongoDbModelStore = new MongoDbModelStore(distanceBasedPersistenceManager.getDb());
        DistanceBasedTwdTransitionRegressorModelFactory distanceBasedTwdTransitionRegressorModelFactory = new DistanceBasedTwdTransitionRegressorModelFactory();
        for (DistanceBasedTwdTransitionRegressorModelMetadata modelMetadata : distanceBasedTwdTransitionRegressorModelFactory
                .getAllValidContextSpecificModelMetadataCandidates()) {
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, DistanceBasedTwdTransitionRegressorModelMetadata> model = distanceBasedTwdTransitionRegressorModelFactory
                    .getNewModel(modelMetadata);
            TwdTransitionRegressorTrainer trainer = new TwdTransitionRegressorTrainer(distanceBasedPersistenceManager,
                    mongoDbModelStore);
            trainer.trainRegressor(model);
        }
        AggregatedSingleDimensionBasedTwdTransitionPersistenceManager durationBasedPersistenceManager = new AggregatedSingleDimensionBasedTwdTransitionPersistenceManager(
                AggregatedSingleDimensionType.DURATION);
        DurationBasedTwdTransitionRegressorModelFactory durationBasedTwdTransitionRegressorModelFactory = new DurationBasedTwdTransitionRegressorModelFactory();
        for (DurationBasedTwdTransitionRegressorModelMetadata modelMetadata : durationBasedTwdTransitionRegressorModelFactory
                .getAllValidContextSpecificModelMetadataCandidates()) {
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata> model = durationBasedTwdTransitionRegressorModelFactory
                    .getNewModel(modelMetadata);
            TwdTransitionRegressorTrainer trainer = new TwdTransitionRegressorTrainer(durationBasedPersistenceManager,
                    mongoDbModelStore);
            trainer.trainRegressor(model);
        }
    }

}
