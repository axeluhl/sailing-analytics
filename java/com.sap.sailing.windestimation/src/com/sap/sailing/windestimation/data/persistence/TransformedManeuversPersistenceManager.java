package com.sap.sailing.windestimation.data.persistence;

public interface TransformedManeuversPersistenceManager<T> extends PersistenceManager<T> {

    void createIfNotExistsCollectionWithTransformedManeuvers();

    void createCollectionWithTransformedManeuvers();

}
