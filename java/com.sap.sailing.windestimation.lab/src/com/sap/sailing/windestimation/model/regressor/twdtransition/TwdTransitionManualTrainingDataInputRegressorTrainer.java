package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager.AggregatedSingleDimensionType;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.MongoDbModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class TwdTransitionManualTrainingDataInputRegressorTrainer {

    private final ModelStore regressorModelStore;
    private final double[][] inputOutputPairs;

    public TwdTransitionManualTrainingDataInputRegressorTrainer(ModelStore regressorModelStore, double[][] inputOutputPairs) {
        this.regressorModelStore = regressorModelStore;
        this.inputOutputPairs = inputOutputPairs;
    }

    public void trainRegressor(
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, ? extends SingleDimensionBasedTwdTransitionRegressorModelMetadata> model)
            throws Exception {
        LoggingUtil
                .logInfo("########## Training of " + model.getContextSpecificModelMetadata().getId() + " started...");
        double[] modelInput = new double[1];
        long numberOfTrainingInstances = 0;
        for (int i = 0; i < inputOutputPairs.length; i++) {
            double xi = inputOutputPairs[i][0];
            if (model.getContextSpecificModelMetadata().isDimensionValueSupported(xi)) {
                double yi = inputOutputPairs[i][1];
                modelInput[0] = xi;
                model.train(modelInput, yi);
                numberOfTrainingInstances++;
            }
        }
        LoggingUtil.logInfo("Calculating root mean square error ...");
        double squareErrorSum = 0;
        int numberOfAggregates = 0;
        for (int i = 0; i < inputOutputPairs.length; i++) {
            double xi = inputOutputPairs[i][0];
            if (model.getContextSpecificModelMetadata().isDimensionValueSupported(xi)) {
                double yi = inputOutputPairs[i][1];
                modelInput[0] = xi;
                double predictedStd = model.getValue(modelInput);
                double diff = predictedStd - yi;
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
            TwdTransitionManualTrainingDataInputRegressorTrainer trainer = new TwdTransitionManualTrainingDataInputRegressorTrainer(
                    mongoDbModelStore, getTrainingDataForDistance());
            trainer.trainRegressor(model);
        }
        DurationBasedTwdTransitionRegressorModelFactory durationBasedTwdTransitionRegressorModelFactory = new DurationBasedTwdTransitionRegressorModelFactory();
        for (DurationBasedTwdTransitionRegressorModelMetadata modelMetadata : durationBasedTwdTransitionRegressorModelFactory
                .getAllValidContextSpecificModelMetadataCandidates()) {
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, DurationBasedTwdTransitionRegressorModelMetadata> model = durationBasedTwdTransitionRegressorModelFactory
                    .getNewModel(modelMetadata);
            TwdTransitionManualTrainingDataInputRegressorTrainer trainer = new TwdTransitionManualTrainingDataInputRegressorTrainer(
                    mongoDbModelStore, getTrainingDataForDuration());
            trainer.trainRegressor(model);
        }
    }

    private static double[][] getTrainingDataForDuration() {
        return new double[][] { { 0, 0 }, { 1, 1 }, { 2, 2 } };
    }

    private static double[][] getTrainingDataForDistance() {
        return new double[][] { { 0, 0 }, { 1, 1 }, { 2, 2 } };
    }

}
