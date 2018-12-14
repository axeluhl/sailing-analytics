package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.ModelFactory;

public interface ClassifierModelFactory<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends ModelFactory<InstanceType, T, TrainableClassificationModel<InstanceType, T>> {

}
