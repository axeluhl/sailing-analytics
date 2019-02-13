package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.AggregatedSingleDimensionBasedTwdTransition;
import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistedElementsIterator;
import com.sap.sailing.windestimation.data.persistence.twdtransition.AggregatedSingleDimensionBasedTwdTransitionPersistenceManager;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class TwdTransitionAggregatedStdRegressorTrainer {

    private final AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager;
    private final ModelStore regressorModelStore;

    public TwdTransitionAggregatedStdRegressorTrainer(
            AggregatedSingleDimensionBasedTwdTransitionPersistenceManager persistenceManager,
            ModelStore regressorModelStore) {
        this.persistenceManager = persistenceManager;
        this.regressorModelStore = regressorModelStore;
    }

    public void trainRegressor(
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, ? extends SingleDimensionBasedTwdTransitionRegressorModelContext> model)
            throws Exception {
        IncrementalSingleDimensionPolynomialRegressorTrainerHelper trainerHelper = new IncrementalSingleDimensionPolynomialRegressorTrainerHelper(
                regressorModelStore, model);
        PersistedElementsIterator<AggregatedSingleDimensionBasedTwdTransition> iterator = persistenceManager
                .getIterator();
        double[] x = new double[1];
        LoggingUtil.logInfo("########## Training of " + model.getModelContext().getId() + " started...");
        // train with x = 0, y = 0
        if (model.getModelContext().isDimensionValueSupportedForTraining(0)) {
            x[0] = model.getModelContext().getPreprocessedDimensionValue(0);
            trainerHelper.incrementalModelTraining(x, 0);
        }
        // train with aggregated twd transitions in MongoDB
        while (iterator.hasNext()) {
            AggregatedSingleDimensionBasedTwdTransition twdTransition = iterator.next();
            double dimensionValue = twdTransition.getDimensionValue();
            if (model.getModelContext().isDimensionValueSupportedForTraining(dimensionValue)) {
                x[0] = model.getModelContext().getPreprocessedDimensionValue(dimensionValue);
                trainerHelper.incrementalModelTraining(x, twdTransition.getZeroMeanStd());
            }
        }
        LoggingUtil.logInfo("Calculating root mean square error ...");
        // evaluate with x = 0, y = 0
        if (model.getModelContext().isDimensionValueSupportedForTraining(0)) {
            x[0] = model.getModelContext().getPreprocessedDimensionValue(0);
            trainerHelper.incrementRmseCalculation(x, 0);
        }
        // evaluate twd transitions in MongoDB
        iterator = persistenceManager.getIterator();
        while (iterator.hasNext()) {
            AggregatedSingleDimensionBasedTwdTransition twdTransition = iterator.next();
            double dimensionValue = twdTransition.getDimensionValue();
            if (model.getModelContext().isDimensionValueSupportedForTraining(dimensionValue)) {
                x[0] = model.getModelContext().getPreprocessedDimensionValue(dimensionValue);
                trainerHelper.incrementRmseCalculation(x, twdTransition.getZeroMeanStd());
            }
        }
        trainerHelper.finishTraining();
    }

}
