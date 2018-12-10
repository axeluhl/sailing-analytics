package com.sap.sailing.windestimation.classifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.windestimation.classifier.store.ClassifierModelStore;

public class ClassifierLoader<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>> {

    private final ClassifierModelStore classifierModelStore;
    private final ClassifierModelFactory<InstanceType, T> classifierModelFactory;

    public ClassifierLoader(ClassifierModelStore classifierModelStore,
            ClassifierModelFactory<InstanceType, T> classifierModelFactory) {
        this.classifierModelStore = classifierModelStore;
        this.classifierModelFactory = classifierModelFactory;
    }

    public TrainableClassificationModel<InstanceType, T> loadBestClassifierModel(
            T contextSpecificModelMetadataWithMaxFeatures) {
        List<T> modelMetadataCandidates = classifierModelFactory
                .getAllValidContextSpecificModelMetadataCandidates(contextSpecificModelMetadataWithMaxFeatures);
        List<TrainableClassificationModel<InstanceType, T>> loadedModels = new ArrayList<>();
        for (T modelMetadata : modelMetadataCandidates) {
            TrainableClassificationModel<InstanceType, T> model = classifierModelFactory
                    .getNewClassifierModel(modelMetadata);
            try {
                model = classifierModelStore.loadPersistedState(model);
                if (model != null && model.hasSupportForProvidedFeatures()) {
                    loadedModels.add(model);
                }
            } catch (ClassifierPersistenceException e) {
            }
        }

        if (loadedModels.isEmpty()) {
            return null;
        }

        Iterator<TrainableClassificationModel<InstanceType, T>> loadedClassifiersIterator = loadedModels.iterator();
        TrainableClassificationModel<InstanceType, T> bestModel = loadedClassifiersIterator.next();
        while (loadedClassifiersIterator.hasNext()) {
            TrainableClassificationModel<InstanceType, T> otherModel = loadedClassifiersIterator.next();
            if (bestModel.getTestScore() < otherModel.getTestScore()) {
                bestModel = otherModel;
            }
        }
        return bestModel;
    }

}
