package com.sap.sailing.windestimation.model.store;

import java.io.Serializable;

public interface PersistableModel extends Serializable {

    PersistenceSupport getPersistenceSupport();

    ContextType getContextType();
}
