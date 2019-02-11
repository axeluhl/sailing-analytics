package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.TrainableModel;

public interface TrainableClassificationModel<InstanceType, T extends ModelContext<InstanceType>>
        extends ClassificationModel<InstanceType, T>, TrainableModel<InstanceType, T> {

    void train(double[][] x, int[] y);

}
