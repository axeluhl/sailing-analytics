package com.sap.sailing.windestimation.data.persistence.maneuver;

public interface TransformedManeuversPersistenceManager<T> extends PersistenceManager<T> {

    void createIfNotExistsCollectionWithTransformedManeuvers();

    void createCollectionWithTransformedManeuvers();

}
