package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;

public interface TrainableRegressorModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends RegressorModel<InstanceType, T>, TrainableModel<InstanceType, T> {

    void train(double[][] x, double[] y);

}
