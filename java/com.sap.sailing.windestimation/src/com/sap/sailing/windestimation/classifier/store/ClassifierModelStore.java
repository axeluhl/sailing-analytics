package com.sap.sailing.windestimation.classifier.store;

import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;

public interface ClassifierModelStore {
    void persistState(TrainableClassificationModel<?, ?> trainedModel) throws ClassifierPersistenceException;

    <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ModelType extends TrainableClassificationModel<InstanceType, T>> ModelType loadPersistedState(
            ModelType newModel) throws ClassifierPersistenceException;

    void delete(TrainableClassificationModel<?, ?> newModel) throws ClassifierPersistenceException;

    void deleteAll(ContextType contextType) throws ClassifierPersistenceException;

    default <InstanceType, T extends ContextSpecificModelMetadata<InstanceType>> PersistenceSupport<TrainableClassificationModel<InstanceType, T>> checkAndGetPersistenceSupport(
            TrainableClassificationModel<InstanceType, T> trainedModel) throws ClassifierPersistenceException {
        PersistenceSupport<TrainableClassificationModel<InstanceType, T>> persistenceSupport = trainedModel
                .getPersistenceSupport();
        if (persistenceSupport == null) {
            throw new ClassifierPersistenceException("Model of type " + trainedModel.getClass().getSimpleName()
                    + " has persistence support: getPersistenceSupport() returned null");
        }
        return persistenceSupport;
    }
}
