package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.AbstractModelCache;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.store.ModelStore;

public abstract class AbstractRegressorCache<InstanceType, T extends ModelContext<InstanceType>>
        extends AbstractModelCache<InstanceType, T, TrainableRegressorModel<InstanceType, T>> {

    public AbstractRegressorCache(ModelStore modelStore, boolean preloadAllModels, long preserveLoadedModelsMillis,
            RegressorModelFactory<InstanceType, T> modelFactory) {
        super(modelStore, preloadAllModels, preserveLoadedModelsMillis, modelFactory);
    }

}
