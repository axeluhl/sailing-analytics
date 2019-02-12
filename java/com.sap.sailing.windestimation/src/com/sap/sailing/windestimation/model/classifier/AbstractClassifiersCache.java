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

    public AbstractClassifiersCache(ModelStore classifierModelStore, boolean preloadAllModels,
            long preserveLoadedClassifiersMillis, ClassifierModelFactory<InstanceType, MC> classifierModelFactory,
            ClassificationResultMapper<InstanceType, MC, ClassificationResultType> classificationResultMapper) {
        super(classifierModelStore, preloadAllModels, preserveLoadedClassifiersMillis, classifierModelFactory);
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
