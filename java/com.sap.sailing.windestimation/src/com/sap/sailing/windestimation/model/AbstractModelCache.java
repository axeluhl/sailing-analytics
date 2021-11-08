package com.sap.sailing.windestimation.model;

import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.domain.maneuverdetection.ShortTimeAfterLastHitCache;
import com.sap.sailing.windestimation.model.store.ModelDomainType;
import com.sap.sailing.windestimation.model.store.ModelStore;

/**
 * Base class for all {@link ModelCache} implementations which uses {@link ShortTimeAfterLastHitCache} as in-memory
 * cache.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of the input instances for models which are managed within this cache instance.
 * @param <MC>
 *            The type of model context associated with models which are managed within this cache instance.
 * @param <ModelType>
 *            The type of the models which are managed within this cache instance.
 */
public abstract class AbstractModelCache<InstanceType, MC extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, MC>>
        implements ModelCache<InstanceType, ModelType> {

    private final ShortTimeAfterLastHitCache<MC, ModelType> modelCache;
    private final ModelLoader<InstanceType, MC, ModelType> modelLoader;
    private final long preserveLoadedModelsMillis;
    private final ModelFactory<InstanceType, MC, ModelType> modelFactory;
    private final boolean preloadAllModels;

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
    public AbstractModelCache(ModelStore modelStore, boolean preloadAllModels, long preserveLoadedModelsMillis,
            ModelFactory<InstanceType, MC, ModelType> modelFactory) {
        this.preloadAllModels = preloadAllModels;
        this.preserveLoadedModelsMillis = preserveLoadedModelsMillis;
        this.modelFactory = modelFactory;
        this.modelCache = new ShortTimeAfterLastHitCache<>(preserveLoadedModelsMillis,
                modelContext -> loadUncachedModel(modelContext));
        this.modelLoader = new ModelLoader<>(modelContext -> modelCache.getCachedValue(modelContext), modelStore,
                modelFactory);
        if (preloadAllModels) {
            preloadAllModels();
        }
    }

    /**
     * Preloads all the models from model store by means of model loader and puts them into the in-memory cache.
     */
    private void preloadAllModels() {
        Map<MC, ModelType> bestModelsPerModelContext = modelLoader.loadBestModelsForAllContexts();
        for (Entry<MC, ModelType> entry : bestModelsPerModelContext.entrySet()) {
            modelCache.addToCache(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Loads best model for model context which is not inside the in-memory cache.
     * 
     * @param modelContext
     *            The model context for which best model will be loaded
     * @return Loaded model, or {@code null} if no suitable model could be found
     */
    private ModelType loadUncachedModel(MC modelContext) {
        ModelType bestModel = modelLoader.loadBestModel(modelContext);
        return bestModel;
    }

    /**
     * Gets best model for the provided model context. If there is not cache entry for the provided model context, the
     * model will be determined using model loader.
     * 
     * @param modelContext
     *            The model context for which best model will be loaded
     * @return Loaded model, or {@code null} if no suitable model could be found
     */
    public ModelType getBestModel(MC modelContext) {
        return modelCache.getValue(modelContext);
    }

    @Override
    public ModelType getBestModel(InstanceType instance) {
        MC modelContext = getModelContext(instance);
        ModelType bestModel = getBestModel(modelContext);
        return bestModel;
    }

    /**
     * Gets the model context which represents the features provided by the given instance.
     */
    public abstract MC getModelContext(InstanceType instance);

    /**
     * If the returned value is not {@link Long#MAX_VALUE}, then the in-memory cache with loaded models will drop models
     * which where not queried for longer than the returned milliseconds. However, an evicted model will be reloaded
     * from model store if it gets queried again.
     */
    public long getPreserveLoadedModelsMillis() {
        return preserveLoadedModelsMillis;
    }

    @Override
    public void clearCache() {
        modelCache.clearCache();
        if (preloadAllModels) {
            preloadAllModels();
        }
    }

    @Override
    public boolean isReady() {
        ModelType omnipresentModel = getBestModel(modelFactory.getModelContextWhichModelAreAlwaysPresent());
        return omnipresentModel != null && omnipresentModel.isModelReady();
    }

    @Override
    public ModelDomainType getModelDomainType() {
        return modelFactory.getModelDomainType();
    }

    @Override
    public boolean isPreloadAllModels() {
        return preloadAllModels;
    }

}
