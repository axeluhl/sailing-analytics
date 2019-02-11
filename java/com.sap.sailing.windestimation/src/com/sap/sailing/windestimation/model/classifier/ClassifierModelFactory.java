package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.ModelFactory;

public interface ClassifierModelFactory<InstanceType, T extends ModelContext<InstanceType>>
        extends ModelFactory<InstanceType, T, TrainableClassificationModel<InstanceType, T>> {

}
