package com.sap.sailing.windestimation.model.regressor.twdtransition;

import java.text.DecimalFormat;

import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.regressor.IncrementalSingleDimensionPolynomialRegressor;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.util.LoggingUtil;

public class IncrementalSingleDimensionPolynomialRegressorTrainerHelper {

    private final ModelStore regressorModelStore;
    private final IncrementalSingleDimensionPolynomialRegressor<?, ?> model;
    private long numberOfTrainingInstances = 0;
    private long numberOfTestInstances = 0;
    private double squareErrorSum = 0;

    public IncrementalSingleDimensionPolynomialRegressorTrainerHelper(ModelStore regressorModelStore,
            IncrementalSingleDimensionPolynomialRegressor<?, ?> model) {
        this.regressorModelStore = regressorModelStore;
        this.model = model;
    }

    public void finishTraining() throws ModelPersistenceException {
        double rootMeanSquareError = Math.sqrt(squareErrorSum / numberOfTestInstances);
        DecimalFormat df = new DecimalFormat("#.###");
        LoggingUtil.logInfo("Root mean square error = " + df.format(rootMeanSquareError));
        LoggingUtil.logInfo("Regressor polynom = " + model.getPolynomAsString());
        model.setTrainingStats(rootMeanSquareError, rootMeanSquareError, numberOfTrainingInstances);
        LoggingUtil.logInfo("Number of training instances: " + numberOfTrainingInstances);
        LoggingUtil.logInfo("Number of test instances: " + numberOfTestInstances);
        LoggingUtil.logInfo("Persisting trained regressor...");
        regressorModelStore.persistModel(model);
        LoggingUtil.logInfo("Regressor persisted successfully. Finished!");
    }

    public void incrementalModelTraining(double[] x, double y) {
        model.train(x, y);
        numberOfTrainingInstances++;
    }

    public void incrementRmseCalculation(double[] x, double y) {
        double predictedStd = model.getValue(x);
        double diff = predictedStd - y;
        squareErrorSum += diff * diff;
        numberOfTestInstances++;
    }

}
