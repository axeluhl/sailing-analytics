package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.TrainableModel;

public interface TrainableRegressorModel<InstanceType, T extends ModelContext<InstanceType>>
        extends RegressorModel<InstanceType, T>, TrainableModel<InstanceType, T> {

    void train(double[][] x, double[] y);

}
