package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.windestimation.data.ManeuverForDataAnalysis;
import com.sap.sailing.windestimation.data.deserializer.ManeuverForDataAnalysisJsonDeserializer;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractTransformedManeuversPersistenceManager;
import com.sap.sailing.windestimation.data.persistence.maneuver.PersistenceManager;

public abstract class AbstractTransformedManeuversForDataAnalysisPersistenceManager
        extends AbstractTransformedManeuversPersistenceManager<ManeuverForDataAnalysis> {

    public AbstractTransformedManeuversForDataAnalysisPersistenceManager(
            PersistenceManager<?>... dependencyToOtherPersistenceManagers) throws UnknownHostException {
        super(dependencyToOtherPersistenceManagers);
    }

    @Override
    protected JsonDeserializer<ManeuverForDataAnalysis> getNewJsonDeserializer() {
        return new ManeuverForDataAnalysisJsonDeserializer();
    }

}
