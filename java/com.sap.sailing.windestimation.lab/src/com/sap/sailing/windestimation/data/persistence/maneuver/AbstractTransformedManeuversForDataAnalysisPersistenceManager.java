package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.windestimation.data.ManeuverForDataAnalysis;
import com.sap.sailing.windestimation.data.serialization.ManeuverForDataAnalysisJsonDeserializer;
import com.sap.sse.shared.json.JsonDeserializer;

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
