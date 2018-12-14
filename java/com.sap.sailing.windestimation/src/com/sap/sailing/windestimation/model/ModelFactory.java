package com.sap.sailing.windestimation.model;

import java.util.List;

public interface ModelFactory<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {

    ModelType getNewClassifierModel(T contextSpecificModelMetadata);

    List<ModelType> getAllTrainableClassifierModels(T contextSpecificModelMetadata);

    List<T> getAllValidContextSpecificModelMetadataCandidates(T contextSpecificModelMetadataWithMaxFeatures);

}
