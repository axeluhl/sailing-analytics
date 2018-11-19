package com.sap.sailing.windestimation.data.persistence.maneuver;

import java.net.UnknownHostException;

import com.sap.sailing.server.gateway.deserialization.impl.DetailedBoatClassJsonDeserializer;
import com.sap.sailing.windestimation.data.ManeuverForDataAnalysis;
import com.sap.sailing.windestimation.data.deserializer.CompetitorTrackWithEstimationDataJsonDeserializer;
import com.sap.sailing.windestimation.data.deserializer.ManeuverForDataAnalysisJsonDeserializer;
import com.sap.sailing.windestimation.data.persistence.maneuver.AbstractRaceWithEstimationDataPersistenceManager;

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
        DetailedBoatClassJsonDeserializer boatClassDeserializer = new DetailedBoatClassJsonDeserializer();
        return new CompetitorTrackWithEstimationDataJsonDeserializer<>(boatClassDeserializer,
                maneuverForClassificationJsonDeserializer);
    }

}