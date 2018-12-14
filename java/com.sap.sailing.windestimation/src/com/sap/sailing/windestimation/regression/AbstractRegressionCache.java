package com.sap.sailing.windestimation.regression;

import com.sap.sailing.windestimation.model.AbstractModelCache;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.store.ModelStore;

public abstract class AbstractRegressionCache<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractModelCache<InstanceType, T, TrainableRegressionModel<InstanceType, T>> {

    public AbstractRegressionCache(ModelStore classifierModelStore, long preserveLoadedClassifiersMillis,
            RegressionModelFactory<InstanceType, T> classifierModelFactory) {
        super(classifierModelStore, preserveLoadedClassifiersMillis, classifierModelFactory);
    }

}
