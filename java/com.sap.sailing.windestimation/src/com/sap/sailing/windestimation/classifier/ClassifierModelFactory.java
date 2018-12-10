package com.sap.sailing.windestimation.classifier;

import java.util.List;

public interface ClassifierModelFactory<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>> {

    TrainableClassificationModel<InstanceType, T> getNewClassifierModel(T contextSpecificModelMetadata);

    List<TrainableClassificationModel<InstanceType, T>> getAllTrainableClassifierModels(T contextSpecificModelMetadata);

    List<T> getAllValidContextSpecificModelMetadataCandidates(T contextSpecificModelMetadataWithMaxFeatures);

}
