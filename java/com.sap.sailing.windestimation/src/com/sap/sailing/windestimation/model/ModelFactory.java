package com.sap.sailing.windestimation.model;

import java.util.List;

import com.sap.sailing.windestimation.model.store.PersistenceContextType;

public interface ModelFactory<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {

    ModelType getNewModel(T contextSpecificModelMetadata);

    List<ModelType> getAllTrainableModels(T contextSpecificModelMetadata);

    List<T> getAllValidContextSpecificModelMetadataFeatureSupersets(T contextSpecificModelMetadataWithMaxFeatures);

    default PersistenceContextType getPersistenceContextType() {
        return getContextSpecificModelMetadataWhichModelIsAlwaysPresentAndHasMinimalFeatures().getContextType();
    }

    T getContextSpecificModelMetadataWhichModelIsAlwaysPresentAndHasMinimalFeatures();

}
