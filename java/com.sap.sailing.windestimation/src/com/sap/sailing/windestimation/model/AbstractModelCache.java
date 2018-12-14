package com.sap.sailing.windestimation.model;

import com.sap.sailing.domain.maneuverdetection.ShortTimeAfterLastHitCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

public abstract class AbstractModelCache<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {

    private final ShortTimeAfterLastHitCache<T, ModelType> modelCache;
    private final ModelLoader<InstanceType, T, ModelType> modelLoader;

    public AbstractModelCache(ModelStore modelStore, long preserveLoadedModelsMillis,
            ModelFactory<InstanceType, T, ModelType> modelFactory) {
        this.modelLoader = new ModelLoader<>(modelStore, modelFactory);
        this.modelCache = new ShortTimeAfterLastHitCache<>(preserveLoadedModelsMillis,
                contextSpecificModelMetadata -> loadModel(contextSpecificModelMetadata));
    }

    private ModelType loadModel(T contextSpecificModelMetadata) {
        ModelType bestModel = modelLoader.loadBestModel(contextSpecificModelMetadata);
        return bestModel;
    }

    public ModelType getBestModel(T contextSpecificModelMetadata) {
        return modelCache.getValue(contextSpecificModelMetadata);
    }

    public ModelType getBestModel(InstanceType instance) {
        T modelMetadata = getContextSpecificModelMetadata(instance);
        ModelType bestModel = getBestModel(modelMetadata);
        return bestModel;
    }

    public abstract T getContextSpecificModelMetadata(InstanceType instance);

}
