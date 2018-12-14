package com.sap.sailing.windestimation.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.windestimation.model.store.ModelStore;

public class ModelLoader<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {

    private final ModelStore modelStore;
    private final ModelFactory<InstanceType, T, ModelType> modelFactory;

    public ModelLoader(ModelStore modelStore, ModelFactory<InstanceType, T, ModelType> modelFactory) {
        this.modelStore = modelStore;
        this.modelFactory = modelFactory;
    }

    public ModelType loadBestModel(T contextSpecificModelMetadataWithMaxFeatures) {
        List<T> modelMetadataCandidates = modelFactory
                .getAllValidContextSpecificModelMetadataCandidates(contextSpecificModelMetadataWithMaxFeatures);
        List<ModelType> loadedModels = new ArrayList<>();
        for (T modelMetadata : modelMetadataCandidates) {
            ModelType model = modelFactory.getNewModel(modelMetadata);
            try {
                model = modelStore.loadPersistedState(model);
                if (model != null && model.hasSupportForProvidedFeatures()) {
                    loadedModels.add(model);
                }
            } catch (ModelPersistenceException e) {
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

}
