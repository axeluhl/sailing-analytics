package com.sap.sailing.windestimation.model.store;

import java.io.InputStream;
import java.io.OutputStream;

import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;

public interface PersistenceSupport {
    String getPersistenceKey();

    void saveToStream(OutputStream output) throws ClassifierPersistenceException;

    PersistableModel loadFromStream(InputStream input) throws ClassifierPersistenceException;
}
