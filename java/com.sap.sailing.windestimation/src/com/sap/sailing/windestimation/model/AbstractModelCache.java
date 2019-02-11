package com.sap.sailing.windestimation.model;

import java.util.List;

import com.sap.sailing.domain.maneuverdetection.ShortTimeAfterLastHitCache;
import com.sap.sailing.windestimation.model.store.ModelStore;
import com.sap.sailing.windestimation.model.store.PersistableModel;
import com.sap.sailing.windestimation.model.store.ModelDomainType;

public abstract class AbstractModelCache<InstanceType, T extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, T>>
        implements ModelCache<InstanceType, ModelType> {

    private final ShortTimeAfterLastHitCache<T, ModelType> modelCache;
    private final ModelLoader<InstanceType, T, ModelType> modelLoader;
    private final long preserveLoadedModelsMillis;
    private final ModelStore modelStore;
    private final ModelFactory<InstanceType, T, ModelType> modelFactory;
    private final boolean preloadAllModels;

    public AbstractModelCache(ModelStore modelStore, boolean preloadAllModels, long preserveLoadedModelsMillis,
            ModelFactory<InstanceType, T, ModelType> modelFactory) {
        this.modelStore = modelStore;
        this.preloadAllModels = preloadAllModels;
        this.preserveLoadedModelsMillis = preserveLoadedModelsMillis;
        this.modelFactory = modelFactory;
        this.modelCache = new ShortTimeAfterLastHitCache<>(preserveLoadedModelsMillis,
                modelContext -> loadModel(modelContext));
        this.modelLoader = new ModelLoader<>(modelContext -> modelCache.getCachedValue(modelContext), modelStore,
                modelFactory);
        if (preloadAllModels) {
            preloadAllModels();
        }
    }

    private void preloadAllModels() {
        List<PersistableModel<?, ?>> loadedModels = modelStore.loadAllPersistedModels(getPersistenceContextType());
        for (PersistableModel<?, ?> persistableModel : loadedModels) {
            @SuppressWarnings("unchecked")
            ModelType loadedModel = (ModelType) persistableModel;
            modelCache.addToCache(loadedModel.getModelContext(), loadedModel);
        }
    }

    protected ModelType loadModel(T modelContext) {
        ModelType bestModel = modelLoader.loadBestModel(modelContext);
        return bestModel;
    }

    public ModelType getBestModel(T modelContext) {
        return modelCache.getValue(modelContext);
    }

    public ModelType getBestModel(InstanceType instance) {
        T modelContext = getModelContext(instance);
        ModelType bestModel = getBestModel(modelContext);
        return bestModel;
    }

    public abstract T getModelContext(InstanceType instance);

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
        ModelType omnipresentModel = getBestModel(
                modelFactory.getContextSpecificModelContextWhichModelIsAlwaysPresentAndHasMinimalFeatures());
        return omnipresentModel != null;
    }

    @Override
    public ModelDomainType getPersistenceContextType() {
        return modelFactory.getPersistenceContextType();
    }

}
