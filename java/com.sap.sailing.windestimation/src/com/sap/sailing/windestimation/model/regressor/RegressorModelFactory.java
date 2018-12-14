package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.ModelFactory;

public interface RegressorModelFactory<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends ModelFactory<InstanceType, T, TrainableRegressorModel<InstanceType, T>> {

}
