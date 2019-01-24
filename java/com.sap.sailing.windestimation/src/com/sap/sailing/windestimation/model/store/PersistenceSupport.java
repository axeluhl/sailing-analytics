package com.sap.sailing.windestimation.model.store;

import java.io.InputStream;
import java.io.OutputStream;

import com.sap.sailing.windestimation.model.exception.ModelPersistenceException;

public interface PersistenceSupport {

    String getId();

    String getPersistenceKey(PersistableModel<?, ?> model);

    void saveToStream(PersistableModel<?, ?> model, OutputStream output) throws ModelPersistenceException;

    PersistableModel<?, ?> loadFromStream(InputStream input) throws ModelPersistenceException;
}
