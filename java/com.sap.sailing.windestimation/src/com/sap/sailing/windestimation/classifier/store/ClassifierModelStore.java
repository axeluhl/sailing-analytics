package com.sap.sailing.windestimation.classifier.store;

import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.TrainableManeuverClassificationModel;

public interface ClassifierModelStore {
    void persistState(TrainableManeuverClassificationModel<?, ?> trainedModel) throws ClassifierPersistenceException;

    boolean loadPersistedState(TrainableManeuverClassificationModel<?, ?> newModel)
            throws ClassifierPersistenceException;
    
    boolean delete(TrainableManeuverClassificationModel<?, ?> newModel) throws ClassifierPersistenceException;
    
    void deleteAll() throws ClassifierPersistenceException;

    default PersistenceSupport checkAndGetPersistenceSupport(TrainableManeuverClassificationModel<?, ?> trainedModel)
            throws ClassifierPersistenceException {
        PersistenceSupport persistenceSupport = trainedModel.getPersistenceSupport();
        if (persistenceSupport == null) {
            throw new ClassifierPersistenceException("Model of type " + trainedModel.getClass().getSimpleName()
                    + " has persistence support: getPersistenceSupport() returned null");
        }
        return persistenceSupport;
    }
}
