package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.windestimation.data.ManeuverForDataAnalysis;
import com.sap.sailing.windestimation.data.serialization.CompetitorTrackWithEstimationDataJsonDeserializer;
import com.sap.sailing.windestimation.data.serialization.ManeuverForDataAnalysisJsonDeserializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class RaceWithManeuverForDataAnalysisPersistenceManager
        extends AbstractRaceWithEstimationDataPersistenceManager<ManeuverForDataAnalysis> {

    public RaceWithManeuverForDataAnalysisPersistenceManager() throws UnknownHostException {
    }

    @Override
    public String getCollectionName() {
        return "racesWithManeuversForDataAnalysis";
    }

    @Override
    public CompetitorTrackWithEstimationDataJsonDeserializer<ManeuverForDataAnalysis> getNewCompetitorTrackWithEstimationDataJsonDeserializer() {
        ManeuverForDataAnalysisJsonDeserializer maneuverForClassificationJsonDeserializer = new ManeuverForDataAnalysisJsonDeserializer();
        BoatClassJsonDeserializer boatClassDeserializer = new BoatClassJsonDeserializer(DomainFactory.INSTANCE);
        return new CompetitorTrackWithEstimationDataJsonDeserializer<>(boatClassDeserializer,
                maneuverForClassificationJsonDeserializer);
    }

}