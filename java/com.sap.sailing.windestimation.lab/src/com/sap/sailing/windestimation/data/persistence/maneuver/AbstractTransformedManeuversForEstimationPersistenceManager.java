package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.windestimation.data.LabeledManeuverForEstimation;
import com.sap.sailing.windestimation.data.serialization.LabeledManeuverForEstimationJsonDeserializer;
import com.sap.sse.shared.json.JsonDeserializer;

public abstract class AbstractTransformedManeuversForEstimationPersistenceManager
        extends AbstractTransformedManeuversPersistenceManager<LabeledManeuverForEstimation> {

    public AbstractTransformedManeuversForEstimationPersistenceManager(
            PersistenceManager<?>... dependencyToOtherPersistenceManagers) throws UnknownHostException {
        super(dependencyToOtherPersistenceManagers);
    }

    @Override
    protected JsonDeserializer<LabeledManeuverForEstimation> getNewJsonDeserializer() {
        return new LabeledManeuverForEstimationJsonDeserializer();
    }

}
