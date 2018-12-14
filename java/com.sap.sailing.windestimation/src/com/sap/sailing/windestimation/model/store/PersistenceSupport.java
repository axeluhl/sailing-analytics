package com.sap.sailing.windestimation.model.store;

import java.io.InputStream;
import java.io.OutputStream;

import com.sap.sailing.windestimation.classifier.ModelPersistenceException;

public interface PersistenceSupport {
    String getPersistenceKey();

    void saveToStream(OutputStream output) throws ModelPersistenceException;

    PersistableModel<?, ?> loadFromStream(InputStream input) throws ModelPersistenceException;
}
