package com.sap.sailing.windestimation.classifier.store;

import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;

public interface ClassifierModelStore {
    void persistState(TrainableClassificationModel<?, ?> trainedModel) throws ClassifierPersistenceException;

    boolean loadPersistedState(TrainableClassificationModel<?, ?> newModel)
            throws ClassifierPersistenceException;
    
    boolean delete(TrainableClassificationModel<?, ?> newModel) throws ClassifierPersistenceException;
    
    void deleteAll() throws ClassifierPersistenceException;

    default PersistenceSupport checkAndGetPersistenceSupport(TrainableClassificationModel<?, ?> trainedModel)
            throws ClassifierPersistenceException {
        PersistenceSupport persistenceSupport = trainedModel.getPersistenceSupport();
        if (persistenceSupport == null) {
            throw new ClassifierPersistenceException("Model of type " + trainedModel.getClass().getSimpleName()
                    + " has persistence support: getPersistenceSupport() returned null");
        }
        return persistenceSupport;
    }
}
