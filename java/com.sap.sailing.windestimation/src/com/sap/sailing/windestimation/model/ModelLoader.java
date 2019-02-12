package com.sap.sailing.windestimation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.PersistableModel;

/**
 * Loads demanded models considering provided {@link ReadableModelCache} and {@link ModelStore}. The main responsibility
 * of this class is to load the most suitable model for a given model context. See more in
 * {@link #loadBestModel(ModelContext)}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of the input instances for models which are loaded by this model loader instance.
 * @param <MC>
 *            The type of model context associated with models loaded by this model loader instance.
 * @param <ModelType>
 *            The type of the models loaded by this model loader instance.
 */
public class ModelLoader<InstanceType, MC extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, MC>> {

    private final ModelStore modelStore;
    private final ModelFactory<InstanceType, MC, ModelType> modelFactory;
    private final ReadableModelCache<InstanceType, MC, ModelType> modelCache;

    /**
     * Constructs a new model loader instance.
     * 
     * @param modelCache
     *            The cache which contains already loaded models by this model loader. Must not be {@code null}.
     * @param modelStore
     *            The model store which will be used if the demanded model is not contained within the provided cache.
     *            {@code null} will disable loading of new uncached models.
     * @param modelFactory
     *            Factory for the models which are loaded by this model loader.
     */
    public ModelLoader(ReadableModelCache<InstanceType, MC, ModelType> modelCache, ModelStore modelStore,
            ModelFactory<InstanceType, MC, ModelType> modelFactory) {
        this.modelCache = modelCache;
        this.modelStore = modelStore;
        this.modelFactory = modelFactory;
    }

    /**
     * Loads the most suitable model for the provided model context. The most suitable model is the model with the
     * highest test score and with associated model context containing the subset of supported features of the provided
     * model context. E.g. if the provided model context includes mark features, and the test score of the model
     * including mark features is lower than of the test score of the model not including mark features, then the latter
     * model is returned. Internally, this method uses {@link #loadModel(ModelContext)} to load models with its test
     * score.
     * 
     * @param modelContextWithMaxFeatures
     *            The model context defining the maximal superset of features to be used. The more features, the more
     *            models will with feature subsets will be considered in terms of its test score.
     * @return The loaded model which is compatible with the provided {@code modelContextWithMaxFeatures} by returning
     *         {@code true} for {@link TrainableModel#hasSupportForProvidedFeatures()}.
     * @throws ModelLoadingException
     *             Thrown when a problem occurs during model loading from persistence layer or its deserialization.
     * @see #loadModel(ModelContext)
     */
    public ModelType loadBestModel(MC modelContextWithMaxFeatures) {
        return loadBestModel(modelContextWithMaxFeatures, modelCache);
    }

    protected ModelType loadBestModel(MC modelContextWithMaxFeatures,
            ReadableModelCache<InstanceType, MC, ModelType> modelCache) {
        List<MC> modelContextCandidates = modelFactory.getAllCompatibleModelContexts(modelContextWithMaxFeatures);
        List<ModelType> loadedModels = new ArrayList<>();
        for (MC modelContext : modelContextCandidates) {
            ModelType loadedModel = loadModel(modelContext, modelCache);
            if (loadedModel != null && loadedModel.isModelReady()) {
                loadedModels.add(loadedModel);
            }
        }
        if (loadedModels.isEmpty()) {
            return null;
        }
        Iterator<ModelType> loadedModelsIterator = loadedModels.iterator();
        ModelType bestModel = loadedModelsIterator.next();
        while (loadedModelsIterator.hasNext()) {
            ModelType otherModel = loadedModelsIterator.next();
            if (bestModel.getTestScore() < otherModel.getTestScore()) {
                bestModel = otherModel;
            }
        }
        return bestModel;
    }

    /**
     * If the model is cached for the provided model context, it will be retrieved from cache, irrespective of the
     * actual model context associated with the cached model. Otherwise, in case model store is not {@code null}, the
     * model with the provided model context will be loaded and deserialized from model store.
     * 
     * @param modelContext
     *            The model with this model context will be loaded.
     * @return The loaded model or {@code null} if not model with the provided model context could be found.
     * @throws ModelLoadingException
     *             Thrown when a problem occurs during model loading from persistence layer or its deserialization.
     */
    public ModelType loadModel(MC modelContext, ReadableModelCache<InstanceType, MC, ModelType> modelCache) {
        ModelType model = modelFactory.getNewModel(modelContext);
        ModelType loadedModel = modelCache == null ? null : modelCache.getModelFromCache(modelContext);
        if (loadedModel == null && modelStore != null) {
            try {
                loadedModel = modelStore.loadModel(model);
            } catch (ModelNotFoundException e) {
                // ignore, because no model might be available for the specified model context
            } catch (ModelPersistenceException e) {
                throw new ModelLoadingException(e);
            }
        }
        return loadedModel;
    }

    /**
     * Loads best models for all model contexts which are contained within the model store.
     * 
     * @return Map with model contexts as keys and corresponding best models as values
     */
    @SuppressWarnings("unchecked")
    public Map<MC, ModelType> loadBestModelsForAllContexts() {
        List<PersistableModel<?, ?>> loadedModels = modelStore
                .loadAllPersistedModels(modelFactory.getModelDomainType());
        Map<MC, ModelType> modelsMap = new HashMap<>(loadedModels.size());
        for (PersistableModel<?, ?> model : loadedModels) {
            modelsMap.put((MC) model.getModelContext(), (ModelType) model);
        }
        ReadableModelCache<InstanceType, MC, ModelType> modelCache = modelContext -> modelsMap.get(modelContext);
        Map<MC, ModelType> result = new HashMap<>(modelsMap.size());
        for (MC modelContext : modelsMap.keySet()) {
            ModelType bestModel = loadBestModel(modelContext, modelCache);
            result.put(modelContext, bestModel);
        }
        return result;
    }

    /**
     * Readable cache with model contexts as keys and already loaded models as values. It is expected that this cache is
     * managed by the user of {@link ModelLoader} by itself.
     * 
     * @author Vladislav Chumak (D069712)
     *
     * @param <InstanceType>
     *            The type of the input instances for models which are cached within this cache instance.
     * @param <MC>
     *            The type of model context associated with models which are cached within this cache instance.
     * @param <ModelType>
     *            The type of the models which are cached within this cache instance.
     */
    public interface ReadableModelCache<InstanceType, MC extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, MC>> {
        ModelType getModelFromCache(MC modelContext);
    }

}
