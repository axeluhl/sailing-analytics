package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.AbstractModelCache;
import com.sap.sailing.windestimation.model.ModelCache;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.store.ModelStore;

/**
 * Specialized {@link ModelCache} for regression models.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of input instances for this model. The purpose of the input instance is to supply the model with
 *            feature vector x, so that the model can generate prediction y.
 * @param <MC>
 *            The type of model context associated with this model.
 */
public abstract class AbstractRegressorCache<InstanceType, MC extends ModelContext<InstanceType>>
        extends AbstractModelCache<InstanceType, MC, TrainableRegressorModel<InstanceType, MC>> {

    public AbstractRegressorCache(ModelStore modelStore, boolean preloadAllModels, long preserveLoadedModelsMillis,
            RegressorModelFactory<InstanceType, MC> modelFactory) {
        super(modelStore, preloadAllModels, preserveLoadedModelsMillis, modelFactory);
    }

}
