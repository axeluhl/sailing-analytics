package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.LabelledManeuverForEstimation;
import com.sap.sailing.windestimation.data.serialization.LabelledManeuverForEstimationJsonDeserializer;

public abstract class AbstractTransformedManeuversForEstimationPersistenceManager
        extends AbstractTransformedManeuversPersistenceManager<LabelledManeuverForEstimation> {

    public AbstractTransformedManeuversForEstimationPersistenceManager(
            PersistenceManager<?>... dependencyToOtherPersistenceManagers) throws UnknownHostException {
        super(dependencyToOtherPersistenceManagers);
    }

    @Override
    protected JsonDeserializer<LabelledManeuverForEstimation> getNewJsonDeserializer() {
        return new LabelledManeuverForEstimationJsonDeserializer();
    }

}
