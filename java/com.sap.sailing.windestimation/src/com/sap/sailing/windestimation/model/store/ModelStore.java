package com.sap.sailing.windestimation.model.store;

import com.sap.sailing.windestimation.classifier.ModelPersistenceException;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.TrainableModel;

public interface ModelStore {
    <T extends PersistableModel<?, ?>> void persistState(T trainedModel) throws ModelPersistenceException;

    <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ModelPersistenceException;

    <T extends PersistableModel<?, ?>> void delete(T newModel) throws ModelPersistenceException;

    void deleteAll(ContextType contextType) throws ModelPersistenceException;

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
