package com.sap.sailing.windestimation.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.windestimation.model.exception.ModelLoadingException;
import com.sap.sailing.windestimation.model.exception.ModelNotFoundException;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class ModelLoader<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {

    private final ModelStore modelStore;
    private final ModelFactory<InstanceType, T, ModelType> modelFactory;
    private ReadableModelCache<InstanceType, T, ModelType> modelCache;

    public ModelLoader(ReadableModelCache<InstanceType, T, ModelType> modelCache, ModelStore modelStore,
            ModelFactory<InstanceType, T, ModelType> modelFactory) {
        this.modelCache = modelCache;
        this.modelStore = modelStore;
        this.modelFactory = modelFactory;
    }

    public ModelType loadBestModel(T contextSpecificModelMetadataWithMaxFeatures) {
        List<T> modelMetadataCandidates = modelFactory
                .getAllValidContextSpecificModelMetadataFeatureSupersets(contextSpecificModelMetadataWithMaxFeatures);
        List<ModelType> loadedModels = new ArrayList<>();
        for (T modelMetadata : modelMetadataCandidates) {
            ModelType loadedModel = loadModel(modelMetadata);
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

    public ModelType loadModel(T contextSpecificModelMetadata) {
        ModelType model = modelFactory.getNewModel(contextSpecificModelMetadata);
        ModelType loadedModel = modelCache == null ? null : modelCache.getModelFromCache(contextSpecificModelMetadata);
        if (loadedModel == null && modelStore != null) {
            try {
                loadedModel = modelStore.loadPersistedState(model);
            } catch (ModelNotFoundException e) {
                // ignore, because no model might be available for the specified model metadata
            } catch (ModelPersistenceException e) {
                throw new ModelLoadingException(e);
            }
        }
        return loadedModel;
    }

    public interface ReadableModelCache<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {
        ModelType getModelFromCache(T contextSpecificModelMetadata);
    }

}
