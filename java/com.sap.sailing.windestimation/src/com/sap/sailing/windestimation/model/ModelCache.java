package com.sap.sailing.windestimation.model;

public interface ModelCache<InstanceType, ModelType extends TrainableModel<InstanceType, ?>> {

    ModelType getBestModel(InstanceType instance);

    void clearCache();
    
    boolean isReady();

}
