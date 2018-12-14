package com.sap.sailing.windestimation.regression;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;

public interface TrainableRegressionModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends RegressionModel<InstanceType, T>, TrainableModel<InstanceType, T> {

    void train(double[][] x, double[] y);

}
