package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.deserializer.ManeuverForEstimationJsonDeserializer;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractTransformedManeuversPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistenceManager;

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
