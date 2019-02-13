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

    /**
     * Constructs a new instance of a model cache.
     * 
     * @param modelStore
     *            The model store containing all trained models which can be loaded in this cache
     * @param preloadAllModels
     *            If {@code true}, all models within the provided model store are loaded inside this cache immediately
     *            within this constructor execution. If {@code false}, the models will be loaded on-demand (lazy
     *            loading).
     * @param preserveLoadedModelsMillis
     *            If not {@link Long#MAX_VALUE}, then the in-memory cache with loaded models will drop models which
     *            where not queried for longer than the provided milliseconds. However, an evicted model will be
     *            reloaded from model store if it gets queried again.
     * @param modelFactory
     *            The model factory which is used to instantiate model instances which are managed by this cache
     */
    public AbstractRegressorCache(ModelStore modelStore, boolean preloadAllModels, long preserveLoadedModelsMillis,
            RegressorModelFactory<InstanceType, MC> modelFactory) {
        super(modelStore, preloadAllModels, preserveLoadedModelsMillis, modelFactory);
    }

}
