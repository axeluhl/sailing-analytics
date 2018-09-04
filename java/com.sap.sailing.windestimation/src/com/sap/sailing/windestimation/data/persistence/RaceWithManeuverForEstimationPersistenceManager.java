package com.sap.sailing.windestimation.data.persistence;

import java.net.UnknownHostException;

import com.sap.sailing.server.gateway.deserialization.impl.DetailedBoatClassJsonDeserializer;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.deserializer.CompetitorTrackWithEstimationDataJsonDeserializer;
import com.sap.sailing.windestimation.data.deserializer.ManeuverForEstimationJsonDeserializer;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class RaceWithManeuverForEstimationPersistenceManager
        extends AbstractRaceWithEstimationDataPersistenceManager<ManeuverForEstimation> {

    public RaceWithManeuverForEstimationPersistenceManager() throws UnknownHostException {
    }

    @Override
    public String getCollectionName() {
        return "racesWithManeuversForEstimation";
    }

    @Override
    public CompetitorTrackWithEstimationDataJsonDeserializer<ManeuverForEstimation> getNewCompetitorTrackWithEstimationDataJsonDeserializer() {
        ManeuverForEstimationJsonDeserializer maneuverForEstimationJsonDeserializer = new ManeuverForEstimationJsonDeserializer();
        DetailedBoatClassJsonDeserializer boatClassDeserializer = new DetailedBoatClassJsonDeserializer();
        return new CompetitorTrackWithEstimationDataJsonDeserializer<>(boatClassDeserializer,
                maneuverForEstimationJsonDeserializer);
    }

}