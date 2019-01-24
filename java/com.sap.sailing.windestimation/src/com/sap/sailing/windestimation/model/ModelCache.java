package com.sap.sailing.windestimation.model;

import com.sap.sailing.windestimation.model.store.PersistenceContextType;

public interface ModelCache<InstanceType, ModelType extends TrainableModel<InstanceType, ?>> {

    ModelType getBestModel(InstanceType instance);

    void clearCache();

    boolean isReady();

    PersistenceContextType getPersistenceContextType();

}
