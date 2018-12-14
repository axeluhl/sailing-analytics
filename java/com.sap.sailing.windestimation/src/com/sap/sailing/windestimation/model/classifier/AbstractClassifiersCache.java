package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.AbstractModelCache;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.store.ModelStore;

public abstract class AbstractClassifiersCache<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ClassificationResultType>
        extends AbstractModelCache<InstanceType, T, TrainableClassificationModel<InstanceType, T>> {

    private final ClassificationResultMapper<InstanceType, T, ClassificationResultType> classificationResultMapper;

    public AbstractClassifiersCache(ModelStore classifierModelStore, long preserveLoadedClassifiersMillis,
            ClassifierModelFactory<InstanceType, T> classifierModelFactory,
            ClassificationResultMapper<InstanceType, T, ClassificationResultType> classificationResultMapper) {
        super(classifierModelStore, preserveLoadedClassifiersMillis, classifierModelFactory);
        this.classificationResultMapper = classificationResultMapper;
    }

    public ClassificationResultType classifyInstance(InstanceType instance) {
        T modelMetadata = getContextSpecificModelMetadata(instance);
        TrainableClassificationModel<InstanceType, T> bestClassifierModel = getBestModel(modelMetadata);
        double[] likelihoods = bestClassifierModel.classifyWithProbabilities(instance);
        ClassificationResultType result = classificationResultMapper.mapToClassificationResult(likelihoods, instance,
                modelMetadata);
        return result;
    }

}
