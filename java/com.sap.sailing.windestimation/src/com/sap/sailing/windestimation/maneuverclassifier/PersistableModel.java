package com.sap.sailing.windestimation.maneuverclassifier;

public interface PersistableModel {

    void loadPersistedModel() throws ClassifierPersistenceException;

    void persistModel() throws ClassifierPersistenceException;

}
