package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

public interface IncrementallyTrainableRegressorModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends TrainableRegressorModel<InstanceType, T> {

    void train(double[] x, double y);

    @Override
    default void train(double[][] x, double[] y) {
        for (int i = 0; i < y.length; i++) {
            double[] inputs = x[i];
            double target = y[i];
            train(inputs, target);
        }
    }

}
