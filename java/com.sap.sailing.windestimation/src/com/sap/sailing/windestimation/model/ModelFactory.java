package com.sap.sailing.windestimation.model;

import java.util.List;

import com.sap.sailing.windestimation.model.store.ModelDomainType;

public interface ModelFactory<InstanceType, T extends ModelContext<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> {

    ModelType getNewModel(T modelContext);

    List<ModelType> getAllTrainableModels(T modelContext);

    List<T> getAllValidModelContexts(T modelContextWithMaxFeatures);

    default ModelDomainType getPersistenceContextType() {
        return getContextSpecificModelContextWhichModelIsAlwaysPresentAndHasMinimalFeatures().getContextType();
    }

    T getContextSpecificModelContextWhichModelIsAlwaysPresentAndHasMinimalFeatures();

}
