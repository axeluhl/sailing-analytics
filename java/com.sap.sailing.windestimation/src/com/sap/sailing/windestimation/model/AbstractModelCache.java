package com.sap.sailing.windestimation.model;

import com.sap.sailing.domain.maneuverdetection.ShortTimeAfterLastHitCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

public abstract class AbstractModelCache<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {

    private final ShortTimeAfterLastHitCache<T, ModelType> classifierCache;
    private final ModelLoader<InstanceType, T, ModelType> classifierLoader;

    public AbstractModelCache(ModelStore classifierModelStore, long preserveLoadedClassifiersMillis,
            ModelFactory<InstanceType, T, ModelType> classifierModelFactory) {
        this.classifierLoader = new ModelLoader<>(classifierModelStore, classifierModelFactory);
        this.classifierCache = new ShortTimeAfterLastHitCache<>(preserveLoadedClassifiersMillis,
                contextSpecificModelMetadata -> loadClassifierModel(contextSpecificModelMetadata));
    }

    private ModelType loadClassifierModel(T contextSpecificModelMetadata) {
        ModelType bestClassifierModel = classifierLoader.loadBestClassifierModel(contextSpecificModelMetadata);
        return bestClassifierModel;
    }

    public ModelType getBestClassifier(T contextSpecificModelMetadata) {
        return classifierCache.getValue(contextSpecificModelMetadata);
    }

    public ModelType getBestClassifier(InstanceType instance) {
        T modelMetadata = getContextSpecificModelMetadata(instance);
        ModelType bestClassifierModel = getBestClassifier(modelMetadata);
        return bestClassifierModel;
    }

    public abstract T getContextSpecificModelMetadata(InstanceType instance);

}
