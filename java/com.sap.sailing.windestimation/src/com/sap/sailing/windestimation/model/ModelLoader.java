package com.sap.sailing.windestimation.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.windestimation.classifier.ModelPersistenceException;
import com.sap.sailing.windestimation.model.store.ModelStore;

public class ModelLoader<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {

    private final ModelStore classifierModelStore;
    private final ModelFactory<InstanceType, T, ModelType> classifierModelFactory;

    public ModelLoader(ModelStore classifierModelStore,
            ModelFactory<InstanceType, T, ModelType> classifierModelFactory) {
        this.classifierModelStore = classifierModelStore;
        this.classifierModelFactory = classifierModelFactory;
    }

    public ModelType loadBestClassifierModel(T contextSpecificModelMetadataWithMaxFeatures) {
        List<T> modelMetadataCandidates = classifierModelFactory
                .getAllValidContextSpecificModelMetadataCandidates(contextSpecificModelMetadataWithMaxFeatures);
        List<ModelType> loadedModels = new ArrayList<>();
        for (T modelMetadata : modelMetadataCandidates) {
            ModelType model = classifierModelFactory.getNewClassifierModel(modelMetadata);
            try {
                model = classifierModelStore.loadPersistedState(model);
                if (model != null && model.hasSupportForProvidedFeatures()) {
                    loadedModels.add(model);
                }
            } catch (ModelPersistenceException e) {
            }
        }

        if (loadedModels.isEmpty()) {
            return null;
        }

        Iterator<ModelType> loadedClassifiersIterator = loadedModels.iterator();
        ModelType bestModel = loadedClassifiersIterator.next();
        while (loadedClassifiersIterator.hasNext()) {
            ModelType otherModel = loadedClassifiersIterator.next();
            if (bestModel.getTestScore() < otherModel.getTestScore()) {
                bestModel = otherModel;
            }
        }
        return bestModel;
    }

}
