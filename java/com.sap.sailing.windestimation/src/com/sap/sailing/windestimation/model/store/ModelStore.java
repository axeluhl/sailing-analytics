package com.sap.sailing.windestimation.model.store;

import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;

public interface ModelStore {
    <T extends PersistableModel> void persistState(T trainedModel) throws ClassifierPersistenceException;

    <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableClassificationModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ClassifierPersistenceException;

    <T extends PersistableModel> void delete(T newModel) throws ClassifierPersistenceException;

    void deleteAll(ContextType contextType) throws ClassifierPersistenceException;

    default <T extends PersistableModel> PersistenceSupport checkAndGetPersistenceSupport(T trainedModel)
            throws ClassifierPersistenceException {
        PersistenceSupport persistenceSupport = trainedModel.getPersistenceSupport();
        if (persistenceSupport == null) {
            throw new ClassifierPersistenceException("Model of type " + trainedModel.getClass().getSimpleName()
                    + " has persistence support: getPersistenceSupport() returned null");
        }
        return persistenceSupport;
    }
}
