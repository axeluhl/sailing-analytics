package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.AbstractModelCache;
import com.sap.sailing.windestimation.model.ModelContext;
import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.store.ModelStore;

public abstract class AbstractClassifiersCache<InstanceType, T extends ModelContext<InstanceType>, ClassificationResultType>
        extends AbstractModelCache<InstanceType, T, TrainableClassificationModel<InstanceType, T>> {

    private final ClassificationResultMapper<InstanceType, T, ClassificationResultType> classificationResultMapper;

    public AbstractClassifiersCache(ModelStore classifierModelStore, boolean preloadAllModels,
            long preserveLoadedClassifiersMillis, ClassifierModelFactory<InstanceType, T> classifierModelFactory,
            ClassificationResultMapper<InstanceType, T, ClassificationResultType> classificationResultMapper) {
        super(classifierModelStore, preloadAllModels, preserveLoadedClassifiersMillis, classifierModelFactory);
        this.classificationResultMapper = classificationResultMapper;
    }

    public ClassificationResultType classifyInstance(InstanceType instance) {
        T modelContext = getModelContext(instance);
        TrainableClassificationModel<InstanceType, T> bestClassifierModel = getBestModel(modelContext);
        if (bestClassifierModel == null) {
            throw new ModelLoadingException("No model available for: " + modelContext);
        }
        double[] likelihoods = bestClassifierModel.classifyWithProbabilities(instance);
        ClassificationResultType result = classificationResultMapper.mapToClassificationResult(likelihoods, instance,
                modelContext);
        return result;
    }

}
