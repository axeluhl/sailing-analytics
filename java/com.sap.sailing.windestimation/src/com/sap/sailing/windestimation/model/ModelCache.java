package com.sap.sailing.windestimation.model;

import com.sap.sailing.windestimation.model.store.ModelDomainType;

/**
 * Cache for {@link TrainableModel} which are automatically loaded on-demand by means of {@link ModelLoader} and cached
 * in-memory for further use.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of the input instances for models which are managed within this cache instance.
 * @param <ModelType>
 *            The type of the models which are managed within this cache instance.
 */
public interface ModelCache<InstanceType, ModelType extends TrainableModel<InstanceType, ?>> {

    /**
     * Gets best model for the given input instance.
     * 
     * @param instance
     *            The instance which will be supported best by the returned model
     * @return The most suitable model, or {@code null} if no suitable model could be found.
     */
    ModelType getBestModel(InstanceType instance);

    /**
     * Clears the cache with already loaded models. If {@link #isPreloadAllModels()} is {@code true}, then the cache
     * will be immediately refilled from the model store provided to this model cache instance.
     */
    void clearCache();

    /**
     * Checks whether the model cache is ready for usage. It is ready if its models are successfully trained and can
     * return a non {@code null} by {@link #getBestModel(Object)} for all possible input instances.
     */
    boolean isReady();

    /**
     * Gets the domain type which is managed by the models within this cache.
     */
    ModelDomainType getModelDomainType();

    /**
     * If {@code true}, then this cache is operating in eager fetching mode meaning that all models are initially
     * preloaded in the in-memory cache. If {@code false}, then this model is operating in the lazy-mode, meaning that
     * the in-memory cache will be filled on-demand only. This configuration flag has also impact on
     * {@link #clearCache()}.
     */
    boolean isPreloadAllModels();

}
