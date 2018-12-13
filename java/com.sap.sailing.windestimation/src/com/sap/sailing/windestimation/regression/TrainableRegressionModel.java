package com.sap.sailing.windestimation.regression;

import com.sap.sailing.windestimation.model.store.PersistableModel;

public interface TrainableRegressionModel<InstanceType> extends RegressionModel<InstanceType>, PersistableModel {

    void train(double[][] x, double[] y);

}
