package com.sap.sailing.windestimation.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class ModelLoader<InstanceType, T extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {

    private final ModelStore modelStore;
    private final ModelFactory<InstanceType, T, ModelType> modelFactory;
    private ReadableModelCache<InstanceType, T, ModelType> modelCache;

    public ModelLoader(ReadableModelCache<InstanceType, T, ModelType> modelCache, ModelStore modelStore,
            ModelFactory<InstanceType, T, ModelType> modelFactory) {
        this.modelCache = modelCache;
        this.modelStore = modelStore;
        this.modelFactory = modelFactory;
    }

    public ModelType loadBestModel(T modelContextWithMaxFeatures) {
        List<T> modelContextCandidates = modelFactory.getAllValidModelContexts(modelContextWithMaxFeatures);
        List<ModelType> loadedModels = new ArrayList<>();
        for (T modelContext : modelContextCandidates) {
            ModelType loadedModel = loadModel(modelContext);
            if (loadedModel != null && loadedModel.hasSupportForProvidedFeatures()) {
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

    public ModelType loadModel(T modelContext) {
        ModelType model = modelFactory.getNewModel(modelContext);
        ModelType loadedModel = modelCache == null ? null : modelCache.getModelFromCache(modelContext);
        if (loadedModel == null && modelStore != null) {
            try {
                loadedModel = modelStore.loadPersistedState(model);
            } catch (ModelNotFoundException e) {
                // ignore, because no model might be available for the specified model context
            } catch (ModelPersistenceException e) {
                throw new ModelLoadingException(e);
            }
        }
        return loadedModel;
    }

    public interface ReadableModelCache<InstanceType, T extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {
        ModelType getModelFromCache(T modelContext);
    }

}
