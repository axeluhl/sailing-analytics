package com.sap.sailing.windestimation.model.store;

public enum PersistenceSupportType {
    SERIALIZATION(new SerializationBasedPersistenceSupport()), NONE(null);

    private final PersistenceSupport persistenceSupport;

    private PersistenceSupportType(PersistenceSupport persistenceSupport) {
        this.persistenceSupport = persistenceSupport;
    }

    public PersistenceSupport getPersistenceSupport() {
        return persistenceSupport;
    }
}
