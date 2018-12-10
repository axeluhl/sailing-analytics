package com.sap.sailing.windestimation.classifier;

import com.sap.sailing.domain.maneuverdetection.ShortTimeAfterLastHitCache;
import com.sap.sailing.windestimation.classifier.store.ClassifierModelStore;

public abstract class AbstractClassifiersCache<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ClassificationResultType> {

    private final ShortTimeAfterLastHitCache<T, TrainableClassificationModel<InstanceType, T>> classifierCache;
    private final ClassifierLoader<InstanceType, T> classifierLoader;
    private final ClassificationResultMapper<InstanceType, T, ClassificationResultType> classificationResultMapper;

    public AbstractClassifiersCache(ClassifierModelStore classifierModelStore, long preserveLoadedClassifiersMillis,
            ClassifierModelFactory<InstanceType, T> classifierModelFactory,
            ClassificationResultMapper<InstanceType, T, ClassificationResultType> classificationResultMapper) {
        this.classifierLoader = new ClassifierLoader<>(classifierModelStore, classifierModelFactory);
        this.classifierCache = new ShortTimeAfterLastHitCache<>(preserveLoadedClassifiersMillis,
                contextSpecificModelMetadata -> loadClassifierModel(contextSpecificModelMetadata));
        this.classificationResultMapper = classificationResultMapper;
    }

    private TrainableClassificationModel<InstanceType, T> loadClassifierModel(T contextSpecificModelMetadata) {
        TrainableClassificationModel<InstanceType, T> bestClassifierModel = classifierLoader
                .loadBestClassifierModel(contextSpecificModelMetadata);
        return bestClassifierModel;
    }

    public TrainableClassificationModel<InstanceType, T> getBestClassifier(T contextSpecificModelMetadata) {
        return classifierCache.getValue(contextSpecificModelMetadata);
    }

    public TrainableClassificationModel<InstanceType, T> getBestClassifier(InstanceType instance) {
        T modelMetadata = getContextSpecificModelMetadata(instance);
        TrainableClassificationModel<InstanceType, T> bestClassifierModel = getBestClassifier(modelMetadata);
        return bestClassifierModel;
    }

    public abstract T getContextSpecificModelMetadata(InstanceType instance);

    public ClassificationResultType classifyInstance(InstanceType instance) {
        T modelMetadata = getContextSpecificModelMetadata(instance);
        TrainableClassificationModel<InstanceType, T> bestClassifierModel = getBestClassifier(modelMetadata);
        double[] x = modelMetadata.getX(instance);
        double[] likelihoods = bestClassifierModel.classifyWithProbabilities(x);
        ClassificationResultType result = classificationResultMapper.mapToClassificationResult(likelihoods, instance,
                modelMetadata);
        return result;
    }

}
