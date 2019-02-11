package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.ModelFactory;

public interface RegressorModelFactory<InstanceType, T extends ModelContext<InstanceType>>
        extends ModelFactory<InstanceType, T, TrainableRegressorModel<InstanceType, T>> {

}
