package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.AbstractModelCache;
import com.sap.sailing.windestimation.model.ModelCache;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.store.ModelStore;

/**
 * Specialized {@link ModelCache} for classification models.
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <InstanceType>
 *            The type of the input instances for models which are managed within this cache instance.
 * @param <MC>
 *            The type of model context associated with models which are managed within this cache instance.
 * @param <ClassificationResultType>
 *            The result type which represents instance classification.
 */
public abstract class AbstractClassifiersCache<InstanceType, MC extends ModelContext<InstanceType>, ClassificationResultType>
        extends AbstractModelCache<InstanceType, MC, TrainableClassificationModel<InstanceType, MC>> {

    private final ClassificationResultMapper<InstanceType, MC, ClassificationResultType> classificationResultMapper;

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
     * @param classificationResultMapper
     *            Mapper which converts the input instances and its classification likelihoods to the demanded return
     *            type.
     */
    public AbstractClassifiersCache(ModelStore modelStore, boolean preloadAllModels, long preserveLoadedModelsMillis,
            ClassifierModelFactory<InstanceType, MC> modelFactory,
            ClassificationResultMapper<InstanceType, MC, ClassificationResultType> classificationResultMapper) {
        super(modelStore, preloadAllModels, preserveLoadedModelsMillis, modelFactory);
        this.classificationResultMapper = classificationResultMapper;
    }

    /**
     * Classifies the provided instance considering its input features.
     * 
     * @param instance
     *            The provided instance with the input features
     * @return Instance classification with likelihoods for each possible classifiable category.
     * @throws ModelLoadingException
     *             If no model is available which supports classification of the provided instance.
     */
    public ClassificationResultType classifyInstance(InstanceType instance) {
        MC modelContext = getModelContext(instance);
        TrainableClassificationModel<InstanceType, MC> bestClassifierModel = getBestModel(modelContext);
        if (bestClassifierModel == null) {
            throw new ModelLoadingException("No model available for: " + modelContext);
        }
        double[] likelihoods = bestClassifierModel.classifyWithProbabilities(instance);
        ClassificationResultType result = classificationResultMapper.mapToClassificationResult(likelihoods, instance,
                modelContext);
        return result;
    }

}
