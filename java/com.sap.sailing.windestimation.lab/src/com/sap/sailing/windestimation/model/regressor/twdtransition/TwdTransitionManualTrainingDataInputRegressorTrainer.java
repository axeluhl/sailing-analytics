package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class TwdTransitionManualTrainingDataInputRegressorTrainer {

    private final double[][] inputOutputPairs;
    private final ModelStore regressorModelStore;

    public TwdTransitionManualTrainingDataInputRegressorTrainer(ModelStore regressorModelStore,
            double[][] inputOutputPairs) {
        this.regressorModelStore = regressorModelStore;
        this.inputOutputPairs = inputOutputPairs;
    }

    public void trainRegressor(
            IncrementalSingleDimensionPolynomialRegressor<TwdTransition, ? extends SingleDimensionBasedTwdTransitionRegressorModelContext> model)
            throws Exception {
        IncrementalSingleDimensionPolynomialRegressorTrainerHelper trainerHelper = new IncrementalSingleDimensionPolynomialRegressorTrainerHelper(
                regressorModelStore, model);
        LoggingUtil.logInfo("########## Training of " + model.getModelContext().getId() + " started...");
        double[] modelInput = new double[1];
        for (int i = 0; i < inputOutputPairs.length; i++) {
            double xi = inputOutputPairs[i][0];
            if (model.getModelContext().isDimensionValueSupportedForTraining(xi)) {
                double yi = inputOutputPairs[i][1];
                modelInput[0] = model.getModelContext().getPreprocessedDimensionValue(xi);
                trainerHelper.incrementalModelTraining(modelInput, yi);
            }
        }
        LoggingUtil.logInfo("Calculating root mean square error ...");
        for (int i = 0; i < inputOutputPairs.length; i++) {
            double xi = inputOutputPairs[i][0];
            if (model.getModelContext().isDimensionValueSupportedForTraining(xi)) {
                double yi = inputOutputPairs[i][1];
                modelInput[0] = model.getModelContext().getPreprocessedDimensionValue(xi);
                trainerHelper.incrementRmseCalculation(modelInput, yi);
            }
        }
        trainerHelper.finishTraining();
    }

}
