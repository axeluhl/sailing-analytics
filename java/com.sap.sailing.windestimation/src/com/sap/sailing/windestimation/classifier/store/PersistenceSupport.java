package com.sap.sailing.windestimation.classifier.store;

import java.io.InputStream;
import java.io.OutputStream;

import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.ModelMetadata;

public interface PersistenceSupport {
    String getPersistenceKey();

    void saveToStream(OutputStream output) throws ClassifierPersistenceException;

    ModelMetadata<?, ?> loadFromStream(InputStream input) throws ClassifierPersistenceException;
}
