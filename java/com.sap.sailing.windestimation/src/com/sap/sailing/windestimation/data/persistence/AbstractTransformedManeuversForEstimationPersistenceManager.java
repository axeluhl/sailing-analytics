package com.sap.sailing.windestimation.data.persistence;

import java.net.UnknownHostException;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.deserializer.ManeuverForEstimationJsonDeserializer;

public abstract class AbstractTransformedManeuversForEstimationPersistenceManager
        extends AbstractTransformedManeuversPersistenceManager<ManeuverForEstimation> {

    public AbstractTransformedManeuversForEstimationPersistenceManager(
            PersistenceManager<?>... dependencyToOtherPersistenceManagers) throws UnknownHostException {
        super(dependencyToOtherPersistenceManagers);
    }

    @Override
    protected JsonDeserializer<ManeuverForEstimation> getNewJsonDeserializer() {
        return new ManeuverForEstimationJsonDeserializer();
    }

}
