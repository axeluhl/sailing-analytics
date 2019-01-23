package com.sap.sailing.windestimation.model;

import com.sap.sailing.domain.maneuverdetection.ShortTimeAfterLastHitCache;
import com.sap.sailing.windestimation.model.store.ModelStore;

public abstract class AbstractModelCache<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>>
        implements ModelCache<InstanceType, ModelType> {

    private final ShortTimeAfterLastHitCache<T, ModelType> modelCache;
    private final ModelLoader<InstanceType, T, ModelType> modelLoader;
    private final long preserveLoadedModelsMillis;
    private final ModelStore modelStore;
    private final ModelFactory<InstanceType, T, ModelType> modelFactory;

    public AbstractModelCache(ModelStore modelStore, long preserveLoadedModelsMillis,
            ModelFactory<InstanceType, T, ModelType> modelFactory) {
        this.modelStore = modelStore;
        this.preserveLoadedModelsMillis = preserveLoadedModelsMillis;
        this.modelFactory = modelFactory;
        this.modelLoader = new ModelLoader<>(modelStore, modelFactory);
        this.modelCache = new ShortTimeAfterLastHitCache<>(preserveLoadedModelsMillis,
                contextSpecificModelMetadata -> loadModel(contextSpecificModelMetadata));
    }

    protected ModelType loadModel(T contextSpecificModelMetadata) {
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

    public long getPreserveLoadedModelsMillis() {
        return preserveLoadedModelsMillis;
    }

    public ModelStore getModelStore() {
        return modelStore;
    }

    @Override
    public void clearCache() {
        modelCache.clearCache();
    }

    @Override
    public boolean isReady() {
        ModelType omnipresentModel = getBestModel(
                getContextSpecificModelMetadataWhichModelIsAlwaysPresentAndHasMinimalFeatures());
        return omnipresentModel != null;
    }

    public abstract T getContextSpecificModelMetadataWhichModelIsAlwaysPresentAndHasMinimalFeatures();

    @Override
    public void preloadAllModels() {
        for (T modelMetadata : modelFactory.getAllValidContextSpecificModelMetadata()) {
            getBestModel(modelMetadata);
        }
    }

}
