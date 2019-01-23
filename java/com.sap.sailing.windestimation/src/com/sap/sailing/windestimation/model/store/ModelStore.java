package com.sap.sailing.windestimation.model.store;

import java.util.Map;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;
import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

public interface ModelStore {
    <T extends PersistableModel<?, ?>> void persistState(T trainedModel) throws ModelPersistenceException;

    <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ModelPersistenceException;

    void deleteAll(PersistenceContextType contextType) throws ModelPersistenceException;

    Map<String, byte[]> exportAllPersistedModels(PersistenceContextType contextType) throws ModelPersistenceException;

    void importPersistedModels(Map<String, byte[]> exportedPersistedModels, PersistenceContextType contextType)
            throws ModelPersistenceException;

    default <T extends PersistableModel<?, ?>> PersistenceSupport checkAndGetPersistenceSupport(T trainedModel)
            throws ModelPersistenceException {
        PersistenceSupport persistenceSupport = trainedModel.getPersistenceSupport();
        if (persistenceSupport == null) {
            throw new ModelPersistenceException("Model of type " + trainedModel.getClass().getSimpleName()
                    + " has persistence support: getPersistenceSupport() returned null");
        }
        return persistenceSupport;
    }
}
