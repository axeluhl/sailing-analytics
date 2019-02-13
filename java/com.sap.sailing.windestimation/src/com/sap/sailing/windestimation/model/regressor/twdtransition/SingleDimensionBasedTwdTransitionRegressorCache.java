package com.sap.sailing.windestimation.model.regressor.twdtransition;

import com.sap.sailing.windestimation.data.TwdTransition;
import com.sap.sailing.windestimation.model.ModelCache;
import com.sap.sailing.windestimation.model.regressor.AbstractRegressorCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

/**
 * {@link ModelCache} which manages either distance-based or duration-based standard deviation of TWD delta regression
 * models which is determined by the provided model factory to the constructor of this class.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleDimensionBasedTwdTransitionRegressorCache<T extends SingleDimensionBasedTwdTransitionRegressorModelContext>
        extends AbstractRegressorCache<TwdTransition, T> {

    private final SingleDimensionBasedTwdTransitionRegressorModelFactory<T> modelFactory;

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
    public SingleDimensionBasedTwdTransitionRegressorCache(ModelStore modelStore, boolean preloadAllModels,
            long preserveLoadedModelsMillis, SingleDimensionBasedTwdTransitionRegressorModelFactory<T> modelFactory) {
        super(modelStore, preloadAllModels, preserveLoadedModelsMillis, modelFactory);
        this.modelFactory = modelFactory;
    }

    @Override
    public T getModelContext(TwdTransition twdTransition) {
        return modelFactory.createNewModelContext(twdTransition);
    }

}
