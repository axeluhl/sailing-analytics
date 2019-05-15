package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

public interface TransformedManeuversPersistenceManager<T> extends PersistenceManager<T> {

    void createIfNotExistsCollectionWithTransformedManeuvers() throws UnknownHostException;

    void createCollectionWithTransformedManeuvers() throws UnknownHostException;

}
