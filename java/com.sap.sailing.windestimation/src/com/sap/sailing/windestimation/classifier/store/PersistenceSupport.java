package com.sap.sailing.windestimation.classifier.store;

import java.io.InputStream;
import java.io.OutputStream;

import com.sap.sailing.windestimation.classifier.ClassifierPersistenceException;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;

public interface PersistenceSupport<T extends TrainableClassificationModel<?, ?>> {
    String getPersistenceKey();

    void saveToStream(OutputStream output) throws ClassifierPersistenceException;

    T loadFromStream(InputStream input) throws ClassifierPersistenceException;
}
