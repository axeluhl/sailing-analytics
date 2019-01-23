package com.sap.sailing.windestimation.model;

import java.util.List;

public interface ModelFactory<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {

    ModelType getNewModel(T contextSpecificModelMetadata);

    List<ModelType> getAllTrainableModels(T contextSpecificModelMetadata);

    List<T> getAllValidContextSpecificModelMetadataFeatureSupersets(T contextSpecificModelMetadataWithMaxFeatures);
    
    List<T> getAllValidContextSpecificModelMetadata();

}
