package com.sap.sailing.windestimation.maneuverclassifier;

import com.sap.sailing.windestimation.maneuverclassifier.impl.smile.ClassifierPersistenceException;

public interface PersistableModel {

    void loadPersistedModel() throws ClassifierPersistenceException;

    void persistModel() throws ClassifierPersistenceException;

}
